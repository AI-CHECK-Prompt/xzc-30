package com.carbon.carbon.controller;

import com.carbon.carbon.service.PricePredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/price")
@CrossOrigin(origins = "*")
public class PricePredictionController {

    @Autowired
    private PricePredictionService pricePredictionService;

    @GetMapping("/prediction")
    public ResponseEntity<Map<String, Object>> predictPriceRange() {
        return ResponseEntity.ok(pricePredictionService.predictPriceRange());
    }

    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> analyzePriceTrend() {
        return ResponseEntity.ok(pricePredictionService.analyzePriceTrend());
    }

    @GetMapping("/liquidity")
    public ResponseEntity<Map<String, Object>> analyzeLiquidity() {
        return ResponseEntity.ok(pricePredictionService.analyzeLiquidity());
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getPriceHistory(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(pricePredictionService.getPriceHistory(days));
    }

    @GetMapping("/anomaly")
    public ResponseEntity<Map<String, Object>> detectPriceAnomaly() {
        return ResponseEntity.ok(pricePredictionService.detectPriceAnomaly());
    }
}
