package com.carbon.carbon.service;

import com.carbon.carbon.entity.ExternalFactor;
import com.carbon.carbon.repository.ExternalFactorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalDataService {

    @Autowired
    private ExternalFactorRepository externalFactorRepository;

    @Transactional
    public ExternalFactor addExternalFactor(LocalDate factorDate, String factorType,
                                            BigDecimal factorValue, String dataSource) {
        ExternalFactor factor = new ExternalFactor();
        factor.setFactorDate(factorDate);
        factor.setFactorType(factorType);
        factor.setFactorValue(factorValue);
        factor.setDataSource(dataSource);
        factor.setCreateTime(LocalDateTime.now());
        return externalFactorRepository.save(factor);
    }

    @Transactional
    public List<ExternalFactor> batchAddExternalFactors(List<ExternalFactor> factors) {
        return externalFactorRepository.saveAll(factors);
    }

    public List<ExternalFactor> getAllExternalFactors() {
        return externalFactorRepository.findAll();
    }

    public List<ExternalFactor> getExternalFactorsByDate(LocalDate startDate, LocalDate endDate) {
        return externalFactorRepository.findByFactorDateBetween(startDate, endDate);
    }

    public List<ExternalFactor> getExternalFactorsByType(String factorType) {
        return externalFactorRepository.findByFactorType(factorType);
    }

    public Map<String, BigDecimal> getLatestFactors() {
        List<String> types = externalFactorRepository.findDistinctFactorTypes();
        Map<String, BigDecimal> latestFactors = new HashMap<>();

        for (String type : types) {
            List<ExternalFactor> factors = externalFactorRepository.findLatestByType(type);
            if (!factors.isEmpty()) {
                latestFactors.put(type, factors.get(0).getFactorValue());
            }
        }
        return latestFactors;
    }

    @Transactional
    public void deleteExternalFactor(Long id) {
        externalFactorRepository.deleteById(id);
    }

    public ExternalFactor getExternalFactorById(Long id) {
        return externalFactorRepository.findById(id).orElse(null);
    }
}
