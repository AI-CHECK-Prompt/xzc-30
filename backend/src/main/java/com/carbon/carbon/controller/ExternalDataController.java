package com.carbon.carbon.controller;

import com.carbon.carbon.entity.ExternalFactor;
import com.carbon.carbon.service.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external-factors")
@CrossOrigin(origins = "*")
public class ExternalDataController {

    @Autowired
    private ExternalDataService externalDataService;

    @PostMapping
    public ResponseEntity<ExternalFactor> addExternalFactor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate factorDate,
            @RequestParam String factorType,
            @RequestParam BigDecimal factorValue,
            @RequestParam(required = false) String dataSource) {

        ExternalFactor factor = externalDataService.addExternalFactor(
                factorDate, factorType, factorValue, dataSource);
        return ResponseEntity.ok(factor);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ExternalFactor>> batchAddExternalFactors(
            @RequestBody List<ExternalFactor> factors) {
        List<ExternalFactor> saved = externalDataService.batchAddExternalFactors(factors);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ExternalFactor>> getAllExternalFactors() {
        return ResponseEntity.ok(externalDataService.getAllExternalFactors());
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, BigDecimal>> getLatestFactors() {
        return ResponseEntity.ok(externalDataService.getLatestFactors());
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<ExternalFactor>> getExternalFactorsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(externalDataService.getExternalFactorsByDate(startDate, endDate));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ExternalFactor>> getExternalFactorsByType(
            @RequestParam String factorType) {
        return ResponseEntity.ok(externalDataService.getExternalFactorsByType(factorType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalFactor> getExternalFactorById(@PathVariable Long id) {
        ExternalFactor factor = externalDataService.getExternalFactorById(id);
        if (factor != null) {
            return ResponseEntity.ok(factor);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExternalFactor(@PathVariable Long id) {
        externalDataService.deleteExternalFactor(id);
        return ResponseEntity.ok().build();
    }
}
