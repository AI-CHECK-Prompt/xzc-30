package com.carbon.carbon.controller;

import com.carbon.carbon.entity.RiskAlert;
import com.carbon.carbon.service.RiskMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskController {

    @Autowired
    private RiskMonitorService riskMonitorService;

    @GetMapping("/alerts/pending")
    public ResponseEntity<List<RiskAlert>> findPendingAlerts() {
        return ResponseEntity.ok(riskMonitorService.findPendingAlerts());
    }

    @GetMapping("/alerts/count")
    public ResponseEntity<Long> countPendingAlerts() {
        return ResponseEntity.ok(riskMonitorService.countPendingAlerts());
    }

    @GetMapping("/alerts/type/{alertType}")
    public ResponseEntity<List<RiskAlert>> findByAlertType(@PathVariable String alertType) {
        return ResponseEntity.ok(riskMonitorService.findByAlertType(alertType));
    }

    @GetMapping("/alerts/level/{alertLevel}")
    public ResponseEntity<List<RiskAlert>> findByAlertLevel(@PathVariable String alertLevel) {
        return ResponseEntity.ok(riskMonitorService.findByAlertLevel(alertLevel));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<RiskAlert>> findAllAlerts() {
        return ResponseEntity.ok(riskMonitorService.findAllAlerts());
    }

    @PostMapping("/alerts/{id}/process")
    public ResponseEntity<RiskAlert> processAlert(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String processingResult = body.get("processingResult").toString();
        return ResponseEntity.ok(riskMonitorService.processAlert(id, processingResult));
    }

    @PostMapping("/check-compliance-gap")
    public ResponseEntity<Void> checkComplianceGap(@RequestBody Map<String, Object> body) {
        Long companyId = Long.parseLong(body.get("companyId").toString());
        Integer year = Integer.parseInt(body.get("year").toString());
        riskMonitorService.checkComplianceGap(companyId, year);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-all-compliance-gap")
    public ResponseEntity<Void> checkAllComplianceGap(@RequestBody Map<String, Object> body) {
        Integer year = Integer.parseInt(body.get("year").toString());
        riskMonitorService.checkAllCompaniesComplianceGap(year);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-position-concentration")
    public ResponseEntity<Void> checkPositionConcentration(@RequestBody Map<String, Object> body) {
        Long companyId = Long.parseLong(body.get("companyId").toString());
        riskMonitorService.checkPositionConcentration(companyId);
        return ResponseEntity.ok().build();
    }
}
