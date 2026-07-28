package com.carbon.carbon.service;

import com.carbon.carbon.repository.CarbonOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PricePredictionService {

    @Autowired
    private CarbonOrderRepository orderRepository;

    private static final int SHORT_TERM_DAYS = 7;
    private static final int MEDIUM_TERM_DAYS = 30;
    private static final BigDecimal VOLATILITY_THRESHOLD = new BigDecimal("0.2");

    public Map<String, Object> predictPriceRange() {
        Map<String, Object> prediction = new HashMap<>();

        Double avgPrice = getHistoricalAveragePrice(SHORT_TERM_DAYS);
        Double volatility = calculateVolatility(SHORT_TERM_DAYS);

        BigDecimal basePrice = BigDecimal.valueOf(avgPrice != null ? avgPrice : 50.0);
        BigDecimal volatilityFactor = BigDecimal.valueOf(volatility != null ? volatility : 0.1);

        BigDecimal lowerBound = basePrice.multiply(BigDecimal.ONE.subtract(volatilityFactor.multiply(BigDecimal.valueOf(1.5))))
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal upperBound = basePrice.multiply(BigDecimal.ONE.add(volatilityFactor.multiply(BigDecimal.valueOf(1.5))))
            .setScale(2, RoundingMode.HALF_UP);

        prediction.put("basePrice", basePrice);
        prediction.put("lowerBound", lowerBound.compareTo(BigDecimal.ZERO) > 0 ? lowerBound : BigDecimal.ZERO);
        prediction.put("upperBound", upperBound);
        prediction.put("volatility", volatilityFactor);
        prediction.put("predictionDate", LocalDateTime.now());
        prediction.put("predictionPeriod", SHORT_TERM_DAYS + "天");

        if (volatilityFactor.compareTo(VOLATILITY_THRESHOLD) > 0) {
            prediction.put("volatilityAlert", "价格波动异常，请关注市场风险");
        }

        return prediction;
    }

    public Map<String, Object> analyzePriceTrend() {
        Map<String, Object> trend = new HashMap<>();

        Double shortTermAvg = getHistoricalAveragePrice(SHORT_TERM_DAYS);
        Double mediumTermAvg = getHistoricalAveragePrice(MEDIUM_TERM_DAYS);

        if (shortTermAvg != null && mediumTermAvg != null) {
            BigDecimal shortTerm = BigDecimal.valueOf(shortTermAvg);
            BigDecimal mediumTerm = BigDecimal.valueOf(mediumTermAvg);

            if (shortTerm.compareTo(mediumTerm) > 0) {
                trend.put("trend", "UP");
                trend.put("trendDescription", "短期价格呈上涨趋势");
            } else if (shortTerm.compareTo(mediumTerm) < 0) {
                trend.put("trend", "DOWN");
                trend.put("trendDescription", "短期价格呈下降趋势");
            } else {
                trend.put("trend", "STABLE");
                trend.put("trendDescription", "短期价格保持稳定");
            }

            BigDecimal changeRate = shortTerm.subtract(mediumTerm)
                .divide(mediumTerm, 4, RoundingMode.HALF_UP)
                .abs();
            trend.put("changeRate", changeRate.multiply(BigDecimal.valueOf(100)));
        } else {
            trend.put("trend", "INSUFFICIENT_DATA");
            trend.put("trendDescription", "数据不足，无法分析趋势");
        }

        return trend;
    }

    public Map<String, Object> analyzeLiquidity() {
        Map<String, Object> liquidity = new HashMap<>();

        LocalDateTime startTime = LocalDateTime.now().minusDays(MEDIUM_TERM_DAYS);
        Double volume = orderRepository.calculateTotalTradingVolume(startTime);
        Long orderCount = orderRepository.findByOrderTimeBetween(startTime, LocalDateTime.now()).stream().count();

        BigDecimal avgDailyVolume = BigDecimal.valueOf(volume != null ? volume : 0)
            .divide(BigDecimal.valueOf(MEDIUM_TERM_DAYS), 2, RoundingMode.HALF_UP);

        liquidity.put("totalVolume", volume != null ? volume : 0);
        liquidity.put("avgDailyVolume", avgDailyVolume);
        liquidity.put("orderCount", orderCount);
        liquidity.put("analysisDate", LocalDateTime.now());

        if (avgDailyVolume.compareTo(new BigDecimal("1000000")) > 0) {
            liquidity.put("liquidityLevel", "HIGH");
            liquidity.put("liquidityDescription", "市场流动性良好");
        } else if (avgDailyVolume.compareTo(new BigDecimal("100000")) > 0) {
            liquidity.put("liquidityLevel", "MEDIUM");
            liquidity.put("liquidityDescription", "市场流动性一般");
        } else {
            liquidity.put("liquidityLevel", "LOW");
            liquidity.put("liquidityDescription", "市场流动性较低");
        }

        return liquidity;
    }

    public List<Map<String, Object>> getPriceHistory(int days) {
        List<Map<String, Object>> history = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = now.minusDays(i).withHour(23).withMinute(59).withSecond(59);

            List<com.carbon.carbon.entity.CarbonOrder> dayOrders =
                orderRepository.findByOrderTimeBetween(dayStart, dayEnd);

            if (!dayOrders.isEmpty()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dayStart.toLocalDate());

                Double avgPrice = dayOrders.stream()
                    .mapToDouble(o -> o.getUnitPrice().doubleValue())
                    .average()
                    .orElse(0.0);
                dayData.put("avgPrice", avgPrice);

                Double maxPrice = dayOrders.stream()
                    .mapToDouble(o -> o.getUnitPrice().doubleValue())
                    .max()
                    .orElse(0.0);
                dayData.put("maxPrice", maxPrice);

                Double minPrice = dayOrders.stream()
                    .mapToDouble(o -> o.getUnitPrice().doubleValue())
                    .min()
                    .orElse(0.0);
                dayData.put("minPrice", minPrice);

                Double totalVolume = dayOrders.stream()
                    .mapToDouble(o -> o.getTotalAmount().doubleValue())
                    .sum();
                dayData.put("volume", totalVolume);

                history.add(dayData);
            }
        }

        return history;
    }

    public Map<String, Object> detectPriceAnomaly() {
        Map<String, Object> anomaly = new HashMap<>();

        Double currentPrice = getHistoricalAveragePrice(1);
        Double historicalAvg = getHistoricalAveragePrice(MEDIUM_TERM_DAYS);

        if (currentPrice != null && historicalAvg != null) {
            BigDecimal current = BigDecimal.valueOf(currentPrice);
            BigDecimal historical = BigDecimal.valueOf(historicalAvg);

            BigDecimal deviation = current.subtract(historical)
                .divide(historical, 4, RoundingMode.HALF_UP)
                .abs();

            anomaly.put("currentPrice", current);
            anomaly.put("historicalAvgPrice", historical);
            anomaly.put("deviation", deviation.multiply(BigDecimal.valueOf(100)));

            if (deviation.compareTo(VOLATILITY_THRESHOLD) > 0) {
                anomaly.put("isAnomaly", true);
                anomaly.put("anomalyDescription", "价格偏离历史均值较大，可能存在异常");
            } else {
                anomaly.put("isAnomaly", false);
                anomaly.put("anomalyDescription", "价格处于正常波动范围");
            }
        } else {
            anomaly.put("isAnomaly", false);
            anomaly.put("anomalyDescription", "数据不足，无法判断");
        }

        return anomaly;
    }

    private Double getHistoricalAveragePrice(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return orderRepository.calculateAveragePrice(startTime);
    }

    private Double calculateVolatility(int days) {
        List<Map<String, Object>> priceHistory = getPriceHistory(days);

        if (priceHistory.size() < 2) {
            return 0.1;
        }

        List<Double> prices = priceHistory.stream()
            .mapToDouble(m -> (double) m.get("avgPrice"))
            .boxed()
            .toList();

        double mean = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = prices.stream()
            .mapToDouble(p -> Math.pow(p - mean, 2))
            .average()
            .orElse(0);

        return Math.sqrt(variance) / mean;
    }

    /**
     * 获取外部因子对价格的影响分析
     */
    public Map<String, Object> analyzeExternalFactorImpact() {
        Map<String, Object> impact = new HashMap<>();
        impact.put("hasExternalFactors", true);
        impact.put("description", "外部因子分析功能待集成Python预测服务后完善");
        return impact;
    }

    /**
     * 综合预测（融合内部+外部数据）
     */
    public Map<String, Object> comprehensivePrediction() {
        Map<String, Object> prediction = predictPriceRange();

        // 添加外部因子分析
        Map<String, Object> factorImpact = analyzeExternalFactorImpact();
        prediction.put("externalFactorImpact", factorImpact);

        // 添加趋势分析
        prediction.put("trendAnalysis", analyzePriceTrend());

        return prediction;
    }
}
