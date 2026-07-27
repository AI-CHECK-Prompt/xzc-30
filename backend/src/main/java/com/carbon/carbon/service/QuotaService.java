package com.carbon.carbon.service;

import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.Quota;
import com.carbon.carbon.repository.CompanyRepository;
import com.carbon.carbon.repository.QuotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuotaService {

    @Autowired
    private QuotaRepository quotaRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public Quota createQuota(Quota quota) {
        quota.setAllocationDate(LocalDateTime.now());
        quota.setCurrentBalance(calculateCurrentBalance(quota));
        Quota savedQuota = quotaRepository.save(quota);

        updateCompanyQuota(savedQuota.getCompany().getId(), savedQuota.getCurrentBalance());
        return savedQuota;
    }

    public Quota updateQuota(Quota quota) {
        quota.setCurrentBalance(calculateCurrentBalance(quota));
        Quota savedQuota = quotaRepository.save(quota);
        updateCompanyQuota(savedQuota.getCompany().getId(), savedQuota.getCurrentBalance());
        return savedQuota;
    }

    public void deleteQuota(Long id) {
        quotaRepository.deleteById(id);
    }

    public Optional<Quota> findById(Long id) {
        return quotaRepository.findById(id);
    }

    public List<Quota> findAll() {
        return quotaRepository.findAll();
    }

    public List<Quota> findByCompany(Long companyId) {
        return companyRepository.findById(companyId)
            .map(quotaRepository::findByCompany)
            .orElse(List.of());
    }

    public List<Quota> findByYear(Integer year) {
        return quotaRepository.findByYear(year);
    }

    public Optional<Quota> findByCompanyAndYear(Long companyId, Integer year) {
        return quotaRepository.findByCompanyIdAndYear(companyId, year);
    }

    public List<Quota> findByComplianceStatus(String complianceStatus) {
        return quotaRepository.findByComplianceStatus(complianceStatus);
    }

    public List<Quota> findNonCompliantByYear(Integer year) {
        return quotaRepository.findNonCompliantByYear(year);
    }

    public Double sumCurrentBalanceByYear(Integer year) {
        Double total = quotaRepository.sumCurrentBalanceByYear(year);
        return total != null ? total : 0.0;
    }

    public Quota allocateAdditionalQuota(Long quotaId, BigDecimal additionalAmount) {
        return quotaRepository.findById(quotaId).map(quota -> {
            BigDecimal current = quota.getAdditionalAllocation() != null ?
                quota.getAdditionalAllocation() : BigDecimal.ZERO;
            quota.setAdditionalAllocation(current.add(additionalAmount));
            quota.setCurrentBalance(calculateCurrentBalance(quota));
            Quota saved = quotaRepository.save(quota);
            updateCompanyQuota(saved.getCompany().getId(), saved.getCurrentBalance());
            return saved;
        }).orElseThrow(() -> new RuntimeException("配额记录不存在"));
    }

    public Quota recordTrade(Long quotaId, BigDecimal quantity, boolean isBuy) {
        return quotaRepository.findById(quotaId).map(quota -> {
            if (isBuy) {
                BigDecimal current = quota.getTradedIn() != null ?
                    quota.getTradedIn() : BigDecimal.ZERO;
                quota.setTradedIn(current.add(quantity));
            } else {
                BigDecimal current = quota.getTradedOut() != null ?
                    quota.getTradedOut() : BigDecimal.ZERO;
                quota.setTradedOut(current.add(quantity));
            }
            quota.setCurrentBalance(calculateCurrentBalance(quota));
            Quota saved = quotaRepository.save(quota);
            updateCompanyQuota(saved.getCompany().getId(), saved.getCurrentBalance());
            return saved;
        }).orElseThrow(() -> new RuntimeException("配额记录不存在"));
    }

    public Quota useOffsetCredit(Long quotaId, BigDecimal offsetAmount) {
        return quotaRepository.findById(quotaId).map(quota -> {
            BigDecimal current = quota.getOffsetUsed() != null ?
                quota.getOffsetUsed() : BigDecimal.ZERO;
            quota.setOffsetUsed(current.add(offsetAmount));
            quota.setCurrentBalance(calculateCurrentBalance(quota));
            Quota saved = quotaRepository.save(quota);
            updateCompanyQuota(saved.getCompany().getId(), saved.getCurrentBalance());
            return saved;
        }).orElseThrow(() -> new RuntimeException("配额记录不存在"));
    }

    public Quota surrenderQuota(Long quotaId, BigDecimal surrenderAmount) {
        return quotaRepository.findById(quotaId).map(quota -> {
            BigDecimal current = quota.getSurrendered() != null ?
                quota.getSurrendered() : BigDecimal.ZERO;
            quota.setSurrendered(current.add(surrenderAmount));
            quota.setComplianceDate(LocalDateTime.now());
            quota.setComplianceStatus("COMPLIANT");
            Quota saved = quotaRepository.save(quota);
            updateCompanyQuota(saved.getCompany().getId(), saved.getCurrentBalance());
            return saved;
        }).orElseThrow(() -> new RuntimeException("配额记录不存在"));
    }

    public void checkAndUpdateComplianceStatus(Long quotaId) {
        quotaRepository.findById(quotaId).ifPresent(quota -> {
            Company company = quota.getCompany();
            if (company.getTotalEmissions() != null && quota.getCurrentBalance() != null) {
                if (quota.getCurrentBalance().compareTo(company.getTotalEmissions()) >= 0) {
                    quota.setComplianceStatus("COMPLIANT");
                } else {
                    quota.setComplianceStatus("NON_COMPLIANT");
                }
                quotaRepository.save(quota);
            }
        });
    }

    private BigDecimal calculateCurrentBalance(Quota quota) {
        BigDecimal initial = quota.getInitialAllocation() != null ?
            quota.getInitialAllocation() : BigDecimal.ZERO;
        BigDecimal additional = quota.getAdditionalAllocation() != null ?
            quota.getAdditionalAllocation() : BigDecimal.ZERO;
        BigDecimal tradedIn = quota.getTradedIn() != null ?
            quota.getTradedIn() : BigDecimal.ZERO;
        BigDecimal tradedOut = quota.getTradedOut() != null ?
            quota.getTradedOut() : BigDecimal.ZERO;
        BigDecimal offset = quota.getOffsetUsed() != null ?
            quota.getOffsetUsed() : BigDecimal.ZERO;
        BigDecimal surrendered = quota.getSurrendered() != null ?
            quota.getSurrendered() : BigDecimal.ZERO;

        return initial.add(additional).add(tradedIn)
            .subtract(tradedOut).subtract(offset).subtract(surrendered);
    }

    private void updateCompanyQuota(Long companyId, BigDecimal quotaHeld) {
        companyRepository.findById(companyId).ifPresent(company -> {
            company.setQuotaHeld(quotaHeld);
            companyRepository.save(company);
        });
    }
}
