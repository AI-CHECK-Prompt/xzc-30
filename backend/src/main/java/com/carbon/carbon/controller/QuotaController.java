package com.carbon.carbon.controller;

import com.carbon.carbon.entity.Quota;
import com.carbon.carbon.service.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotas")
@CrossOrigin(origins = "*")
public class QuotaController {

    @Autowired
    private QuotaService quotaService;

    @PostMapping
    public ResponseEntity<Quota> createQuota(@RequestBody Quota quota) {
        return ResponseEntity.ok(quotaService.createQuota(quota));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quota> updateQuota(@PathVariable Long id, @RequestBody Quota quota) {
        quota.setId(id);
        return ResponseEntity.ok(quotaService.updateQuota(quota));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuota(@PathVariable Long id) {
        quotaService.deleteQuota(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quota> findById(@PathVariable Long id) {
        return quotaService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Quota>> findAll() {
        return ResponseEntity.ok(quotaService.findAll());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Quota>> findByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(quotaService.findByCompany(companyId));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<Quota>> findByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(quotaService.findByYear(year));
    }

    @GetMapping("/company/{companyId}/year/{year}")
    public ResponseEntity<Quota> findByCompanyAndYear(@PathVariable Long companyId, @PathVariable Integer year) {
        return quotaService.findByCompanyAndYear(companyId, year)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/compliance-status/{status}")
    public ResponseEntity<List<Quota>> findByComplianceStatus(@PathVariable String status) {
        return ResponseEntity.ok(quotaService.findByComplianceStatus(status));
    }

    @GetMapping("/non-compliant/{year}")
    public ResponseEntity<List<Quota>> findNonCompliantByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(quotaService.findNonCompliantByYear(year));
    }

    @GetMapping("/total-balance/{year}")
    public ResponseEntity<Double> sumCurrentBalanceByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(quotaService.sumCurrentBalanceByYear(year));
    }

    @PostMapping("/{id}/additional-allocation")
    public ResponseEntity<Quota> allocateAdditionalQuota(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        return ResponseEntity.ok(quotaService.allocateAdditionalQuota(id, amount));
    }

    @PostMapping("/{id}/trade")
    public ResponseEntity<Quota> recordTrade(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        Boolean isBuy = Boolean.parseBoolean(body.get("isBuy").toString());
        return ResponseEntity.ok(quotaService.recordTrade(id, quantity, isBuy));
    }

    @PostMapping("/{id}/offset")
    public ResponseEntity<Quota> useOffsetCredit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal offsetAmount = new BigDecimal(body.get("offsetAmount").toString());
        return ResponseEntity.ok(quotaService.useOffsetCredit(id, offsetAmount));
    }

    @PostMapping("/{id}/surrender")
    public ResponseEntity<Quota> surrenderQuota(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal surrenderAmount = new BigDecimal(body.get("surrenderAmount").toString());
        return ResponseEntity.ok(quotaService.surrenderQuota(id, surrenderAmount));
    }

    @PostMapping("/{id}/check-compliance")
    public ResponseEntity<Void> checkComplianceStatus(@PathVariable Long id) {
        quotaService.checkAndUpdateComplianceStatus(id);
        return ResponseEntity.ok().build();
    }
}
