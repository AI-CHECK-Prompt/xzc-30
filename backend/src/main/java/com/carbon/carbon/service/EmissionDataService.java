package com.carbon.carbon.service;

import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.EmissionData;
import com.carbon.carbon.entity.User;
import com.carbon.carbon.repository.CompanyRepository;
import com.carbon.carbon.repository.EmissionDataRepository;
import com.carbon.carbon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmissionDataService {

    @Autowired
    private EmissionDataRepository emissionDataRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    public EmissionData createEmissionData(EmissionData emissionData) {
        emissionData.setReportingDate(LocalDateTime.now());
        emissionData.setDataStatus("SUBMITTED");
        emissionData.setVerificationStatus("PENDING");

        if (emissionData.getTotalEmissions() == null && emissionData.getDirectEmissions() != null) {
            BigDecimal indirect = emissionData.getIndirectEmissions() != null ?
                emissionData.getIndirectEmissions() : BigDecimal.ZERO;
            emissionData.setTotalEmissions(emissionData.getDirectEmissions().add(indirect));
        }

        return emissionDataRepository.save(emissionData);
    }

    public EmissionData updateEmissionData(EmissionData emissionData) {
        return emissionDataRepository.save(emissionData);
    }

    public void deleteEmissionData(Long id) {
        emissionDataRepository.deleteById(id);
    }

    public Optional<EmissionData> findById(Long id) {
        return emissionDataRepository.findById(id);
    }

    public List<EmissionData> findAll() {
        return emissionDataRepository.findAll();
    }

    public List<EmissionData> findByCompany(Long companyId) {
        return companyRepository.findById(companyId)
            .map(emissionDataRepository::findByCompany)
            .orElse(List.of());
    }

    public List<EmissionData> findByYear(Integer year) {
        return emissionDataRepository.findByYear(year);
    }

    public Optional<EmissionData> findByCompanyAndYear(Long companyId, Integer year) {
        return emissionDataRepository.findByCompanyIdAndYear(companyId, year);
    }

    public List<EmissionData> findPendingVerification() {
        return emissionDataRepository.findPendingVerification();
    }

    public EmissionData verifyEmissionData(Long dataId, Long verifierId, String status, String remarks) {
        Optional<EmissionData> optData = emissionDataRepository.findById(dataId);
        Optional<User> optVerifier = userRepository.findById(verifierId);

        if (optData.isPresent() && optVerifier.isPresent()) {
            EmissionData data = optData.get();
            data.setVerificationStatus(status);
            data.setVerifier(optVerifier.get());
            data.setVerificationDate(LocalDateTime.now());
            data.setRemarks(remarks);
            return emissionDataRepository.save(data);
        }
        throw new RuntimeException("排放数据或审核员不存在");
    }

    public Double sumTotalEmissionsByYear(Integer year) {
        Double total = emissionDataRepository.sumTotalEmissionsByYear(year);
        return total != null ? total : 0.0;
    }

    public void calculateEmissionIntensity(Long dataId) {
        emissionDataRepository.findById(dataId).ifPresent(data -> {
            if (data.getTotalEmissions() != null && data.getCompany() != null) {
                BigDecimal base = BigDecimal.ONE;
                if (data.getCompany().getIndustry() != null
                        && data.getCompany().getIndustry().getIntensityBase() != null) {
                    base = data.getCompany().getIndustry().getIntensityBase();
                }
                BigDecimal intensity = data.getTotalEmissions()
                    .divide(base, 6, RoundingMode.HALF_UP);
                data.setEmissionIntensity(intensity);
                emissionDataRepository.save(data);
            }
        });
    }

    public List<EmissionData> findByVerificationStatus(String status) {
        return emissionDataRepository.findByVerificationStatus(status);
    }
}
