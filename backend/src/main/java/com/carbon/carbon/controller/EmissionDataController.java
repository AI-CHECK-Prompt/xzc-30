package com.carbon.carbon.controller;

import com.carbon.carbon.entity.EmissionData;
import com.carbon.carbon.service.EmissionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emissions")
@CrossOrigin(origins = "*")
public class EmissionDataController {

    @Autowired
    private EmissionDataService emissionDataService;

    @PostMapping
    public ResponseEntity<EmissionData> createEmissionData(@RequestBody EmissionData emissionData) {
        return ResponseEntity.ok(emissionDataService.createEmissionData(emissionData));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmissionData> updateEmissionData(@PathVariable Long id, @RequestBody EmissionData emissionData) {
        emissionData.setId(id);
        return ResponseEntity.ok(emissionDataService.updateEmissionData(emissionData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmissionData(@PathVariable Long id) {
        emissionDataService.deleteEmissionData(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmissionData> findById(@PathVariable Long id) {
        return emissionDataService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EmissionData>> findAll() {
        return ResponseEntity.ok(emissionDataService.findAll());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<EmissionData>> findByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(emissionDataService.findByCompany(companyId));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<EmissionData>> findByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(emissionDataService.findByYear(year));
    }

    @GetMapping("/company/{companyId}/year/{year}")
    public ResponseEntity<EmissionData> findByCompanyAndYear(@PathVariable Long companyId, @PathVariable Integer year) {
        return emissionDataService.findByCompanyAndYear(companyId, year)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<List<EmissionData>> findPendingVerification() {
        return ResponseEntity.ok(emissionDataService.findPendingVerification());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<EmissionData> verifyEmissionData(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long verifierId = Long.parseLong(body.get("verifierId").toString());
        String status = body.get("status").toString();
        String remarks = body.getOrDefault("remarks", "").toString();
        return ResponseEntity.ok(emissionDataService.verifyEmissionData(id, verifierId, status, remarks));
    }

    @GetMapping("/total-emissions/{year}")
    public ResponseEntity<Double> sumTotalEmissionsByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(emissionDataService.sumTotalEmissionsByYear(year));
    }

    @GetMapping("/verification-status/{status}")
    public ResponseEntity<List<EmissionData>> findByVerificationStatus(@PathVariable String status) {
        return ResponseEntity.ok(emissionDataService.findByVerificationStatus(status));
    }
}
