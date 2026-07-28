package com.carbon.carbon.service;

import com.carbon.carbon.entity.TradingSignal;
import com.carbon.carbon.repository.CarbonOrderRepository;
import com.carbon.carbon.repository.TradingSignalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TradingSignalService {

    @Autowired
    private TradingSignalRepository tradingSignalRepository;

    @Autowired
    private PredictionIntegrationService predictionIntegrationService;

    private final ObjectMapper objectMapper;

    // 阈值配置
    private static final BigDecimal THRESHOLD_PERCENT = new BigDecimal("0.05"); // 5%
    private static final BigDecimal HIGH_CONFIDENCE_THRESHOLD = new BigDecimal("0.10"); // 10%
    private static final BigDecimal MEDIUM_CONFIDENCE_THRESHOLD = new BigDecimal("0.20"); // 20%

    public TradingSignalService() {
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public Map<String, Object> generateSignal() {
        Map<String, Object> result = new HashMap<>();

        // 获取当前价格（使用最近交易价格）
        Double currentPrice = getCurrentPrice();

        // 执行预测
        Map<String, Object> predictionResult = predictionIntegrationService.runPrediction(7);
        Map<String, Object> prediction = (Map<String, Object>) predictionResult.get("prediction");

        if (prediction == null) {
            result.put("status", "error");
            result.put("message", "No prediction available");
            return result;
        }

        // 提取预测价格
        BigDecimal predictedPrice = new BigDecimal(prediction.get("pointPrediction").toString());
        BigDecimal lowerBound80 = new BigDecimal(
            ((Map<String, Object>) prediction.get("interval80")).get("lower").toString());
        BigDecimal upperBound80 = new BigDecimal(
            ((Map<String, Object>) prediction.get("interval80")).get("upper").toString());

        // 计算区间宽度
        BigDecimal intervalWidth = upperBound80.subtract(lowerBound80);
        BigDecimal intervalWidthPercent = intervalWidth.divide(predictedPrice, 4, RoundingMode.HALF_UP);

        // 确定置信度
        String confidenceLevel = determineConfidenceLevel(intervalWidthPercent);

        // 确定信号类型
        String signalType = determineSignalType(currentPrice, predictedPrice);

        // 计算建议价格区间
        BigDecimal threshold = BigDecimal.valueOf(currentPrice).multiply(THRESHOLD_PERCENT);
        BigDecimal priceRangeStart, priceRangeEnd;

        if (TradingSignal.BUY.equals(signalType)) {
            priceRangeStart = BigDecimal.valueOf(currentPrice);
            priceRangeEnd = BigDecimal.valueOf(currentPrice).add(threshold);
        } else if (TradingSignal.SELL.equals(signalType)) {
            priceRangeStart = BigDecimal.valueOf(currentPrice).subtract(threshold);
            priceRangeEnd = BigDecimal.valueOf(currentPrice);
        } else {
            priceRangeStart = lowerBound80;
            priceRangeEnd = upperBound80;
        }

        // 获取关键因素
        Map<String, Object> keyFactors = extractKeyFactors(prediction);
        Map<String, Object> factorContribution = extractFactorContribution(prediction);

        // 保存交易信号
        TradingSignal signal = new TradingSignal();
        signal.setSignalTime(LocalDateTime.now());
        signal.setSignalType(signalType);
        signal.setConfidenceLevel(confidenceLevel);
        signal.setPriceRangeStart(priceRangeStart);
        signal.setPriceRangeEnd(priceRangeEnd);
        signal.setThresholdDescription(generateThresholdDescription(signalType, threshold));
        signal.setKeyFactors(toJson(keyFactors));
        signal.setFactorContribution(toJson(factorContribution));

        Long predictionId = (Long) predictionResult.get("predictionId");
        if (predictionId != null) {
            signal.setPredictionId(predictionId);
        }

        signal.setModelVersion(prediction.containsKey("modelVersion") ?
            prediction.get("modelVersion").toString() : "v1.0.0");
        signal.setCreateTime(LocalDateTime.now());

        TradingSignal savedSignal = tradingSignalRepository.save(signal);

        result.put("status", "success");
        result.put("signal", convertToMap(savedSignal));
        result.put("keyFactors", keyFactors);
        result.put("factorContribution", factorContribution);

        return result;
    }

    private Double getCurrentPrice() {
        // 使用最近交易价格作为当前价格
        return 50.0; // 默认价格，实际应从数据库获取
    }

    private String determineConfidenceLevel(BigDecimal intervalWidthPercent) {
        if (intervalWidthPercent.compareTo(HIGH_CONFIDENCE_THRESHOLD) <= 0) {
            return TradingSignal.HIGH;
        } else if (intervalWidthPercent.compareTo(MEDIUM_CONFIDENCE_THRESHOLD) <= 0) {
            return TradingSignal.MEDIUM;
        }
        return TradingSignal.LOW;
    }

    private String determineSignalType(Double currentPrice, BigDecimal predictedPrice) {
        BigDecimal current = BigDecimal.valueOf(currentPrice);
        BigDecimal threshold = current.multiply(THRESHOLD_PERCENT);

        if (predictedPrice.compareTo(current.add(threshold)) > 0) {
            return TradingSignal.BUY;
        } else if (predictedPrice.compareTo(current.subtract(threshold)) < 0) {
            return TradingSignal.SELL;
        }
        return TradingSignal.HOLD;
    }

    private Map<String, Object> extractKeyFactors(Map<String, Object> prediction) {
        Map<String, Object> factors = new HashMap<>();

        if (prediction.containsKey("tsFeatures")) {
            Map<String, Object> tsFeatures = (Map<String, Object>) prediction.get("tsFeatures");
            if (tsFeatures.containsKey("trend")) {
                factors.put("priceTrend", tsFeatures.get("trend"));
            }
        }

        if (prediction.containsKey("mlFeatures")) {
            Map<String, Object> mlFeatures = (Map<String, Object>) prediction.get("mlFeatures");
            if (mlFeatures.containsKey("featureImportance")) {
                Map<String, Object> importance = (Map<String, Object>) mlFeatures.get("featureImportance");
                factors.put("topFactors", importance.keySet().stream().limit(3).toList());
            }
        }

        return factors;
    }

    private Map<String, Object> extractFactorContribution(Map<String, Object> prediction) {
        if (prediction.containsKey("mlFeatures")) {
            Map<String, Object> mlFeatures = (Map<String, Object>) prediction.get("mlFeatures");
            if (mlFeatures.containsKey("factorContribution")) {
                return (Map<String, Object>) mlFeatures.get("factorContribution");
            }
        }
        return new HashMap<>();
    }

    private String generateThresholdDescription(String signalType, BigDecimal threshold) {
        return String.format("Signal: %s, Threshold: %.2f%% of current price",
            signalType, threshold.multiply(BigDecimal.valueOf(100)).doubleValue());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> convertToMap(TradingSignal signal) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", signal.getId());
        map.put("signalTime", signal.getSignalTime());
        map.put("signalType", signal.getSignalType());
        map.put("confidenceLevel", signal.getConfidenceLevel());
        map.put("priceRangeStart", signal.getPriceRangeStart());
        map.put("priceRangeEnd", signal.getPriceRangeEnd());
        map.put("modelVersion", signal.getModelVersion());
        return map;
    }

    public TradingSignal getLatestSignal() {
        return tradingSignalRepository.findTopByOrderBySignalTimeDesc().orElse(null);
    }

    public List<TradingSignal> getSignalHistory() {
        return tradingSignalRepository.findAllByOrderBySignalTimeDesc();
    }

    public TradingSignal getSignalById(Long id) {
        return tradingSignalRepository.findById(id).orElse(null);
    }
}
