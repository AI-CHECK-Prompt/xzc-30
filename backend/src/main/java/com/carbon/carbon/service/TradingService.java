package com.carbon.carbon.service;

import com.carbon.carbon.entity.CarbonOrder;
import com.carbon.carbon.entity.Company;
import com.carbon.carbon.repository.CarbonOrderRepository;
import com.carbon.carbon.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TradingService {

    @Autowired
    private CarbonOrderRepository orderRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private QuotaService quotaService;

    @Autowired
    private RiskMonitorService riskMonitorService;

    private static final String TRADING_MODE_LISTING = "LISTING";
    private static final String TRADING_MODE_TRANSFER = "TRANSFER";
    private static final String TRADING_MODE_AUCTION = "AUCTION";

    private static final String ORDER_TYPE_BUY = "BUY";
    private static final String ORDER_TYPE_SELL = "SELL";

    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_MATCHED = "MATCHED";
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    private static final String SETTLEMENT_STATUS_UNSETTLED = "UNSETTLED";
    private static final String SETTLEMENT_STATUS_SETTLED = "SETTLED";

    public CarbonOrder createListingOrder(Long sellerId, BigDecimal quantity, BigDecimal unitPrice) {
        Company seller = companyRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("卖方企业不存在"));

        validateSellerQuota(seller, quantity);

        CarbonOrder order = new CarbonOrder();
        order.setOrderNo(generateOrderNo("LIST"));
        order.setSeller(seller);
        order.setOrderType(ORDER_TYPE_SELL);
        order.setTradingMode(TRADING_MODE_LISTING);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(quantity.multiply(unitPrice));
        order.setOrderStatus(ORDER_STATUS_PENDING);
        order.setSettlementStatus(SETTLEMENT_STATUS_UNSETTLED);
        order.setOrderTime(LocalDateTime.now());

        riskMonitorService.checkLargeTransaction(order);
        riskMonitorService.checkRelatedPartyTransaction(order);

        return orderRepository.save(order);
    }

    public CarbonOrder createBuyListingOrder(Long buyerId, BigDecimal quantity, BigDecimal unitPrice) {
        Company buyer = companyRepository.findById(buyerId)
            .orElseThrow(() -> new RuntimeException("买方企业不存在"));

        CarbonOrder order = new CarbonOrder();
        order.setOrderNo(generateOrderNo("BUY"));
        order.setBuyer(buyer);
        order.setOrderType(ORDER_TYPE_BUY);
        order.setTradingMode(TRADING_MODE_LISTING);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(quantity.multiply(unitPrice));
        order.setOrderStatus(ORDER_STATUS_PENDING);
        order.setSettlementStatus(SETTLEMENT_STATUS_UNSETTLED);
        order.setOrderTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public CarbonOrder createTransferOrder(Long sellerId, Long buyerId, BigDecimal quantity, BigDecimal unitPrice) {
        Company seller = companyRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("卖方企业不存在"));
        Company buyer = companyRepository.findById(buyerId)
            .orElseThrow(() -> new RuntimeException("买方企业不存在"));

        validateSellerQuota(seller, quantity);

        CarbonOrder order = new CarbonOrder();
        order.setOrderNo(generateOrderNo("TRF"));
        order.setSeller(seller);
        order.setBuyer(buyer);
        order.setOrderType(ORDER_TYPE_SELL);
        order.setTradingMode(TRADING_MODE_TRANSFER);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(quantity.multiply(unitPrice));
        order.setOrderStatus(ORDER_STATUS_PENDING);
        order.setSettlementStatus(SETTLEMENT_STATUS_UNSETTLED);
        order.setOrderTime(LocalDateTime.now());

        riskMonitorService.checkRelatedPartyTransaction(order);

        return orderRepository.save(order);
    }

    public CarbonOrder createAuctionOrder(Long sellerId, BigDecimal quantity, BigDecimal startPrice) {
        Company seller = companyRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("卖方企业不存在"));

        validateSellerQuota(seller, quantity);

        CarbonOrder order = new CarbonOrder();
        order.setOrderNo(generateOrderNo("AUC"));
        order.setSeller(seller);
        order.setOrderType(ORDER_TYPE_SELL);
        order.setTradingMode(TRADING_MODE_AUCTION);
        order.setQuantity(quantity);
        order.setUnitPrice(startPrice);
        order.setTotalAmount(quantity.multiply(startPrice));
        order.setOrderStatus(ORDER_STATUS_PENDING);
        order.setSettlementStatus(SETTLEMENT_STATUS_UNSETTLED);
        order.setOrderTime(LocalDateTime.now());

        riskMonitorService.checkLargeTransaction(order);

        return orderRepository.save(order);
    }

    public CarbonOrder submitAuctionBid(Long orderId, Long bidderId, BigDecimal bidPrice) {
        CarbonOrder auctionOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("拍卖订单不存在"));

        if (!TRADING_MODE_AUCTION.equals(auctionOrder.getTradingMode())) {
            throw new RuntimeException("该订单不是拍卖订单");
        }

        Company bidder = companyRepository.findById(bidderId)
            .orElseThrow(() -> new RuntimeException("竞买方不存在"));

        if (bidPrice.compareTo(auctionOrder.getUnitPrice()) > 0) {
            auctionOrder.setBuyer(bidder);
            auctionOrder.setUnitPrice(bidPrice);
            auctionOrder.setTotalAmount(auctionOrder.getQuantity().multiply(bidPrice));
        }

        return orderRepository.save(auctionOrder);
    }

    public CarbonOrder matchOrder(Long orderId) {
        CarbonOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        if (TRADING_MODE_LISTING.equals(order.getTradingMode())) {
            return matchListingOrder(order);
        } else if (TRADING_MODE_TRANSFER.equals(order.getTradingMode())) {
            return matchTransferOrder(order);
        } else if (TRADING_MODE_AUCTION.equals(order.getTradingMode())) {
            return matchAuctionOrder(order);
        }

        throw new RuntimeException("不支持的交易模式");
    }

    private CarbonOrder matchListingOrder(CarbonOrder order) {
        List<CarbonOrder> matchingOrders;

        if (ORDER_TYPE_SELL.equals(order.getOrderType())) {
            matchingOrders = orderRepository.findByOrderType(ORDER_TYPE_BUY).stream()
                .filter(o -> ORDER_STATUS_PENDING.equals(o.getOrderStatus()))
                .filter(o -> o.getUnitPrice().compareTo(order.getUnitPrice()) >= 0)
                .toList();
        } else {
            matchingOrders = orderRepository.findByOrderType(ORDER_TYPE_SELL).stream()
                .filter(o -> ORDER_STATUS_PENDING.equals(o.getOrderStatus()))
                .filter(o -> order.getUnitPrice().compareTo(o.getUnitPrice()) >= 0)
                .toList();
        }

        if (!matchingOrders.isEmpty()) {
            CarbonOrder matchedOrder = matchingOrders.get(0);
            executeTrade(order, matchedOrder);
            return matchedOrder;
        }

        order.setOrderStatus(ORDER_STATUS_MATCHED);
        return orderRepository.save(order);
    }

    private CarbonOrder matchTransferOrder(CarbonOrder order) {
        if (order.getBuyer() == null) {
            throw new RuntimeException("协议转让需要指定买方");
        }
        return executeTrade(order, order);
    }

    private CarbonOrder matchAuctionOrder(CarbonOrder order) {
        if (order.getBuyer() == null) {
            throw new RuntimeException("拍卖无人出价");
        }
        return executeTrade(order, order);
    }

    private CarbonOrder executeTrade(CarbonOrder sellOrder, CarbonOrder matchOrder) {
        CarbonOrder trade = new CarbonOrder();
        trade.setOrderNo(generateOrderNo("TRD"));
        trade.setSeller(sellOrder.getSeller());
        trade.setBuyer(matchOrder.getBuyer());
        trade.setOrderType(ORDER_TYPE_SELL);
        trade.setTradingMode(sellOrder.getTradingMode());
        trade.setQuantity(sellOrder.getQuantity());
        trade.setUnitPrice(sellOrder.getUnitPrice());
        trade.setTotalAmount(sellOrder.getTotalAmount());
        trade.setOrderStatus(ORDER_STATUS_COMPLETED);
        trade.setSettlementStatus(SETTLEMENT_STATUS_SETTLED);
        trade.setOrderTime(sellOrder.getOrderTime());
        trade.setMatchTime(LocalDateTime.now());
        trade.setSettlementTime(LocalDateTime.now());

        updateQuotasForTrade(trade);

        // 取消被成交的原始挂单
        sellOrder.setOrderStatus(ORDER_STATUS_CANCELLED);
        orderRepository.save(sellOrder);

        // 取消匹配方的原始挂单
        matchOrder.setOrderStatus(ORDER_STATUS_CANCELLED);
        orderRepository.save(matchOrder);

        riskMonitorService.checkPositionConcentration(trade.getBuyer().getId());

        return orderRepository.save(trade);
    }

    private void updateQuotasForTrade(CarbonOrder trade) {
        quotaService.findByCompanyAndYear(trade.getSeller().getId(), LocalDateTime.now().getYear())
            .ifPresent(quota -> quotaService.recordTrade(quota.getId(), trade.getQuantity(), false));

        quotaService.findByCompanyAndYear(trade.getBuyer().getId(), LocalDateTime.now().getYear())
            .ifPresent(quota -> quotaService.recordTrade(quota.getId(), trade.getQuantity(), true));
    }

    public CarbonOrder cancelOrder(Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            if (!ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
                throw new RuntimeException("只能取消待匹配状态的订单");
            }
            order.setOrderStatus(ORDER_STATUS_CANCELLED);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    public Optional<CarbonOrder> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<CarbonOrder> findAll() {
        return orderRepository.findAll();
    }

    public List<CarbonOrder> findByStatus(String status) {
        return orderRepository.findByOrderStatus(status);
    }

    public List<CarbonOrder> findByTradingMode(String mode) {
        return orderRepository.findByTradingMode(mode);
    }

    public Double getAveragePrice(Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        Double avg = orderRepository.calculateAveragePrice(startTime);
        return avg != null ? avg : 0.0;
    }

    public Double getTotalTradingVolume(Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        Double total = orderRepository.calculateTotalTradingVolume(startTime);
        return total != null ? total : 0.0;
    }

    public List<CarbonOrder> findRecentOrders(Integer hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return orderRepository.findByOrderTimeBetween(startTime, LocalDateTime.now());
    }

    private void validateSellerQuota(Company seller, BigDecimal quantity) {
        if (seller.getQuotaHeld() == null || seller.getQuotaHeld().compareTo(quantity) < 0) {
            throw new RuntimeException("企业配额不足");
        }
    }

    private String generateOrderNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
