package com.carbon.carbon.controller;

import com.carbon.carbon.entity.CarbonOrder;
import com.carbon.carbon.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
@CrossOrigin(origins = "*")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    @PostMapping("/listing/sell")
    public ResponseEntity<CarbonOrder> createSellListing(@RequestBody Map<String, Object> body) {
        Long sellerId = Long.parseLong(body.get("sellerId").toString());
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        BigDecimal unitPrice = new BigDecimal(body.get("unitPrice").toString());
        return ResponseEntity.ok(tradingService.createListingOrder(sellerId, quantity, unitPrice));
    }

    @PostMapping("/listing/buy")
    public ResponseEntity<CarbonOrder> createBuyListing(@RequestBody Map<String, Object> body) {
        Long buyerId = Long.parseLong(body.get("buyerId").toString());
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        BigDecimal unitPrice = new BigDecimal(body.get("unitPrice").toString());
        return ResponseEntity.ok(tradingService.createBuyListingOrder(buyerId, quantity, unitPrice));
    }

    @PostMapping("/transfer")
    public ResponseEntity<CarbonOrder> createTransfer(@RequestBody Map<String, Object> body) {
        Long sellerId = Long.parseLong(body.get("sellerId").toString());
        Long buyerId = Long.parseLong(body.get("buyerId").toString());
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        BigDecimal unitPrice = new BigDecimal(body.get("unitPrice").toString());
        return ResponseEntity.ok(tradingService.createTransferOrder(sellerId, buyerId, quantity, unitPrice));
    }

    @PostMapping("/auction")
    public ResponseEntity<CarbonOrder> createAuction(@RequestBody Map<String, Object> body) {
        Long sellerId = Long.parseLong(body.get("sellerId").toString());
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        BigDecimal startPrice = new BigDecimal(body.get("startPrice").toString());
        return ResponseEntity.ok(tradingService.createAuctionOrder(sellerId, quantity, startPrice));
    }

    @PostMapping("/auction/{orderId}/bid")
    public ResponseEntity<CarbonOrder> submitBid(@PathVariable Long orderId, @RequestBody Map<String, Object> body) {
        Long bidderId = Long.parseLong(body.get("bidderId").toString());
        BigDecimal bidPrice = new BigDecimal(body.get("bidPrice").toString());
        return ResponseEntity.ok(tradingService.submitAuctionBid(orderId, bidderId, bidPrice));
    }

    @PostMapping("/match/{orderId}")
    public ResponseEntity<CarbonOrder> matchOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(tradingService.matchOrder(orderId));
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<CarbonOrder> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(tradingService.cancelOrder(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarbonOrder> findById(@PathVariable Long id) {
        return tradingService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarbonOrder>> findAll() {
        return ResponseEntity.ok(tradingService.findAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CarbonOrder>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(tradingService.findByStatus(status));
    }

    @GetMapping("/mode/{mode}")
    public ResponseEntity<List<CarbonOrder>> findByTradingMode(@PathVariable String mode) {
        return ResponseEntity.ok(tradingService.findByTradingMode(mode));
    }

    @GetMapping("/avg-price")
    public ResponseEntity<Double> getAveragePrice(@RequestParam(defaultValue = "7") Integer days) {
        return ResponseEntity.ok(tradingService.getAveragePrice(days));
    }

    @GetMapping("/total-volume")
    public ResponseEntity<Double> getTotalTradingVolume(@RequestParam(defaultValue = "7") Integer days) {
        return ResponseEntity.ok(tradingService.getTotalTradingVolume(days));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<CarbonOrder>> findRecentOrders(@RequestParam(defaultValue = "24") Integer hours) {
        return ResponseEntity.ok(tradingService.findRecentOrders(hours));
    }
}
