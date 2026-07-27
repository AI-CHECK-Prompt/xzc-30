package com.carbon.carbon.controller;

import com.carbon.carbon.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        return ResponseEntity.ok(dashboardService.getMarketOverview());
    }

    @GetMapping("/industry-distribution")
    public ResponseEntity<List<Map<String, Object>>> getIndustryDistribution() {
        return ResponseEntity.ok(dashboardService.getIndustryDistribution());
    }

    @GetMapping("/trading-heatmap")
    public ResponseEntity<Map<String, Object>> getTradingHeatMap() {
        return ResponseEntity.ok(dashboardService.getTradingHeatMap());
    }

    @GetMapping("/compliance-progress")
    public ResponseEntity<Map<String, Object>> getComplianceProgress() {
        return ResponseEntity.ok(dashboardService.getComplianceProgress());
    }

    @GetMapping("/price-trend")
    public ResponseEntity<List<Map<String, Object>>> getPriceTrendData(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getPriceTrendData(days));
    }

    @GetMapping("/risk-alerts-summary")
    public ResponseEntity<List<Map<String, Object>>> getRiskAlertsSummary() {
        return ResponseEntity.ok(dashboardService.getRiskAlertsSummary());
    }

    @GetMapping("/liquidity-indicators")
    public ResponseEntity<Map<String, Object>> getLiquidityIndicators() {
        return ResponseEntity.ok(dashboardService.getLiquidityIndicators());
    }

    @GetMapping("/top-companies")
    public ResponseEntity<List<Map<String, Object>>> getTopCompaniesByEmissions() {
        return ResponseEntity.ok(dashboardService.getTopCompaniesByEmissions());
    }
}
