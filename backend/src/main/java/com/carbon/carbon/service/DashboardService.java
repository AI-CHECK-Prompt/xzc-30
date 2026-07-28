package com.carbon.carbon.service;

import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.Industry;
import com.carbon.carbon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private IndustryRepository industryRepository;

    @Autowired
    private CarbonOrderRepository orderRepository;

    @Autowired
    private EmissionDataRepository emissionDataRepository;

    @Autowired
    private QuotaRepository quotaRepository;

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    public Map<String, Object> getMarketOverview() {
        Map<String, Object> overview = new HashMap<>();

        Long totalCompanies = companyRepository.count();
        overview.put("totalCompanies", totalCompanies);

        List<Industry> industries = industryRepository.findAll();
        overview.put("totalIndustries", industries.size());

        Long pendingAlerts = riskAlertRepository.countPendingAlerts();
        overview.put("pendingAlerts", pendingAlerts);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        Double todayVolume = orderRepository.calculateTotalQuantity(startOfDay);
        overview.put("todayVolume", todayVolume != null ? todayVolume : 0);

        Double avgPrice = orderRepository.calculateAveragePrice(startOfDay);
        overview.put("todayAvgPrice", avgPrice != null ? avgPrice : 0);

        Long todayOrders = orderRepository.findByOrderTimeBetween(startOfDay, now).stream().count();
        overview.put("todayOrders", todayOrders);

        return overview;
    }

    public List<Map<String, Object>> getIndustryDistribution() {
        List<Map<String, Object>> distribution = new ArrayList<>();

        List<Industry> industries = industryRepository.findAll();
        for (Industry industry : industries) {
            Map<String, Object> data = new HashMap<>();
            data.put("industryCode", industry.getCode());
            data.put("industryName", industry.getName());

            Long count = companyRepository.countByIndustryId(industry.getId());
            data.put("companyCount", count);

            List<Company> companies = companyRepository.findByIndustryId(industry.getId());
            BigDecimal totalEmissions = companies.stream()
                .map(Company::getTotalEmissions)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.put("totalEmissions", totalEmissions);

            distribution.add(data);
        }

        return distribution;
    }

    public Map<String, Object> getTradingHeatMap() {
        Map<String, Object> heatMap = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        List<com.carbon.carbon.entity.CarbonOrder> recentOrders =
            orderRepository.findByOrderTimeBetween(now.minusDays(7), now);

        Map<String, Long> tradingByHour = new HashMap<>();
        Map<String, Long> tradingByDay = new HashMap<>();

        for (com.carbon.carbon.entity.CarbonOrder order : recentOrders) {
            String hour = String.valueOf(order.getOrderTime().getHour());
            String day = order.getOrderTime().getDayOfWeek().name();

            tradingByHour.merge(hour, 1L, Long::sum);
            tradingByDay.merge(day, 1L, Long::sum);
        }

        heatMap.put("byHour", tradingByHour);
        heatMap.put("byDay", tradingByDay);
        heatMap.put("totalTransactions", recentOrders.size());

        return heatMap;
    }

    public Map<String, Object> getComplianceProgress() {
        Map<String, Object> progress = new HashMap<>();

        List<Company> allCompanies = companyRepository.findAll();
        long total = allCompanies.size();

        long compliant = allCompanies.stream()
            .filter(c -> "COMPLIANT".equals(c.getComplianceStatus()))
            .count();

        long nonCompliant = allCompanies.stream()
            .filter(c -> "NON_COMPLIANT".equals(c.getComplianceStatus()))
            .count();

        long pending = total - compliant - nonCompliant;

        progress.put("total", total);
        progress.put("compliant", compliant);
        progress.put("nonCompliant", nonCompliant);
        progress.put("pending", pending);
        progress.put("complianceRate", total > 0 ?
            BigDecimal.valueOf(compliant * 100.0 / total).setScale(2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO);

        return progress;
    }

    public List<Map<String, Object>> getPriceTrendData(int days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = now.minusDays(i).withHour(23).withMinute(59).withSecond(59);

            List<com.carbon.carbon.entity.CarbonOrder> dayOrders =
                orderRepository.findByOrderTimeBetween(dayStart, dayEnd);

            Map<String, Object> data = new HashMap<>();
            data.put("date", dayStart.toLocalDate().toString());

            if (!dayOrders.isEmpty()) {
                Double avgPrice = dayOrders.stream()
                    .mapToDouble(o -> o.getUnitPrice().doubleValue())
                    .average()
                    .orElse(0.0);
                data.put("avgPrice", avgPrice);

                Double volume = dayOrders.stream()
                    .mapToDouble(o -> o.getTotalAmount().doubleValue())
                    .sum();
                data.put("volume", volume);

                data.put("orderCount", dayOrders.size());
            } else {
                data.put("avgPrice", 0);
                data.put("volume", 0);
                data.put("orderCount", 0);
            }

            trendData.add(data);
        }

        return trendData;
    }

    public List<Map<String, Object>> getRiskAlertsSummary() {
        List<Map<String, Object>> summary = new ArrayList<>();

        List<String> alertTypes = Arrays.asList(
            "LARGE_TRANSACTION", "RELATED_PARTY", "POSITION_CONCENTRATION",
            "ABNORMAL_TRADING", "COMPLIANCE_GAP"
        );

        for (String type : alertTypes) {
            List<com.carbon.carbon.entity.RiskAlert> alerts =
                riskAlertRepository.findByAlertType(type);

            long pending = alerts.stream()
                .filter(a -> "PENDING".equals(a.getAlertStatus()))
                .count();
            long processed = alerts.stream()
                .filter(a -> "PROCESSED".equals(a.getAlertStatus()))
                .count();

            Map<String, Object> data = new HashMap<>();
            data.put("alertType", type);
            data.put("total", alerts.size());
            data.put("pending", pending);
            data.put("processed", processed);

            summary.add(data);
        }

        return summary;
    }

    public Map<String, Object> getLiquidityIndicators() {
        Map<String, Object> indicators = new HashMap<>();

        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        Double totalVolume = orderRepository.calculateTotalTradingVolume(startTime);
        Long orderCount = orderRepository.findByOrderTimeBetween(startTime, LocalDateTime.now()).stream().count();

        Double avgPrice = orderRepository.calculateAveragePrice(startTime);

        indicators.put("totalVolume30Days", totalVolume != null ? totalVolume : 0);
        indicators.put("orderCount30Days", orderCount);
        indicators.put("avgPrice30Days", avgPrice != null ? avgPrice : 0);
        indicators.put("avgDailyVolume", totalVolume != null ? totalVolume / 30 : 0);

        if (orderCount > 0) {
            indicators.put("avgOrderSize", totalVolume / orderCount);
        } else {
            indicators.put("avgOrderSize", 0);
        }

        return indicators;
    }

    public List<Map<String, Object>> getTopCompaniesByEmissions() {
        List<Company> companies = companyRepository.findAll();

        return companies.stream()
            .sorted((a, b) -> {
                BigDecimal aEmissions = a.getTotalEmissions() != null ? a.getTotalEmissions() : BigDecimal.ZERO;
                BigDecimal bEmissions = b.getTotalEmissions() != null ? b.getTotalEmissions() : BigDecimal.ZERO;
                return bEmissions.compareTo(aEmissions);
            })
            .limit(10)
            .map(company -> {
                Map<String, Object> data = new HashMap<>();
                data.put("companyId", company.getId());
                data.put("companyName", company.getName());
                data.put("industry", company.getIndustry() != null ? company.getIndustry().getName() : "");
                data.put("totalEmissions", company.getTotalEmissions());
                data.put("quotaHeld", company.getQuotaHeld());
                data.put("complianceStatus", company.getComplianceStatus());
                return data;
            })
            .collect(Collectors.toList());
    }
}
