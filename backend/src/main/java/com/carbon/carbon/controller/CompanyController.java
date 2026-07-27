package com.carbon.carbon.controller;

import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.Industry;
import com.carbon.carbon.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        return ResponseEntity.ok(companyService.createCompany(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        company.setId(id);
        return ResponseEntity.ok(companyService.updateCompany(company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> findById(@PathVariable Long id) {
        return companyService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Company>> findAll() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @GetMapping("/industry/{industryId}")
    public ResponseEntity<List<Company>> findByIndustry(@PathVariable Long industryId) {
        return ResponseEntity.ok(companyService.findByIndustry(industryId));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<Company>> findByRegion(@PathVariable String region) {
        return ResponseEntity.ok(companyService.findByRegion(region));
    }

    @GetMapping("/compliance/{status}")
    public ResponseEntity<List<Company>> findByComplianceStatus(@PathVariable String status) {
        return ResponseEntity.ok(companyService.findByComplianceStatus(status));
    }

    @GetMapping("/quota-deficit/{year}")
    public ResponseEntity<List<Company>> findCompaniesWithQuotaDeficit(@PathVariable Integer year) {
        return ResponseEntity.ok(companyService.findCompaniesWithQuotaDeficit(year));
    }

    @GetMapping("/industries")
    public ResponseEntity<List<Industry>> findAllIndustries() {
        return ResponseEntity.ok(companyService.findAllIndustries());
    }

    @PostMapping("/init-industries")
    public ResponseEntity<String> initIndustries() {
        companyService.initIndustries();
        return ResponseEntity.ok("行业初始化完成");
    }

    @PatchMapping("/{id}/quota-held")
    public ResponseEntity<Void> updateQuotaHeld(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        companyService.updateQuotaHeld(id, new java.math.BigDecimal(body.get("quotaHeld").toString()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/compliance-status")
    public ResponseEntity<Void> updateComplianceStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        companyService.updateComplianceStatus(id, body.get("status").toString());
        return ResponseEntity.ok().build();
    }
}
