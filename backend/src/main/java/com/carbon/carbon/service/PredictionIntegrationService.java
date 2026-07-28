package com.carbon.carbon.service;

import com.carbon.carbon.entity.PredictionResult;
import com.carbon.carbon.repository.PredictionResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionIntegrationService {

    @Value("${prediction.service.url:http://localhost:8000}")
    private String predictionServiceUrl;

    @Autowired
    private PredictionResultRepository predictionResultRepository;

    @Autowired
    private PricePredictionService pricePredictionService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PredictionIntegrationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> runPrediction(Integer horizon) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Python预测服务
            String url = predictionServiceUrl + "/predict";
            Map<String, Object> request = new HashMap<>();
            request.put("horizon", horizon != null ? horizon : 7);
            request.put("train", true);

            Map<String, Object> response = restTemplate.postForObject(
                url, request, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                // 保存预测结果
                PredictionResult predictionResult = savePredictionResult(data, horizon);
                result.put("predictionId", predictionResult.getId());
                result.put("prediction", data);
                result.put("status", "success");
            } else {
                result.put("status", "error");
                result.put("message", "Prediction service returned error");
            }

        } catch (Exception e) {
            // 如果Python服务不可用，使用本地预测
            result.put("status", "fallback");
            result.put("message", "Python service unavailable, using local prediction");
            result.put("prediction", pricePredictionService.predictPriceRange());
        }

        return result;
    }

    private PredictionResult savePredictionResult(Map<String, Object> data, Integer horizon) {
        PredictionResult result = new PredictionResult();
        result.setPredictionTime(LocalDateTime.now());
        result.setPredictionHorizon(horizon != null ? horizon : 7);

        if (data.containsKey("pointPrediction")) {
            result.setPredictedPrice(new BigDecimal(data.get("pointPrediction").toString()));
        }

        Map<String, Object> interval80 = (Map<String, Object>) data.get("interval80");
        if (interval80 != null) {
            result.setLowerBound80(new BigDecimal(interval80.get("lower").toString()));
            result.setUpperBound80(new BigDecimal(interval80.get("upper").toString()));
        }

        Map<String, Object> interval95 = (Map<String, Object>) data.get("interval95");
        if (interval95 != null) {
            result.setLowerBound95(new BigDecimal(interval95.get("lower").toString()));
            result.setUpperBound95(new BigDecimal(interval95.get("upper").toString()));
        }

        result.setModelVersion(data.containsKey("modelVersion") ?
            data.get("modelVersion").toString() : "v1.0.0");

        result.setCreateTime(LocalDateTime.now());

        return predictionResultRepository.save(result);
    }

    public PredictionResult getPredictionResult(Long id) {
        return predictionResultRepository.findById(id).orElse(null);
    }

    public Map<String, Object> getPredictionHistory() {
        Map<String, Object> result = new HashMap<>();
        result.put("predictions", predictionResultRepository.findAllByOrderByPredictionTimeDesc());
        return result;
    }
}
