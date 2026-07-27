package com.carbon.carbon.service;

import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.Industry;
import com.carbon.carbon.repository.CompanyRepository;
import com.carbon.carbon.repository.IndustryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private IndustryRepository industryRepository;

    public Company createCompany(Company company) {
        company.setRegistrationDate(LocalDateTime.now());
        company.setStatus("ACTIVE");
        return companyRepository.save(company);
    }

    public Company updateCompany(Company company) {
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public List<Company> findByIndustry(Long industryId) {
        return companyRepository.findByIndustryId(industryId);
    }

    public List<Company> findByRegion(String region) {
        return companyRepository.findByRegion(region);
    }

    public List<Company> findByComplianceStatus(String complianceStatus) {
        return companyRepository.findByComplianceStatus(complianceStatus);
    }

    public List<Company> findCompaniesWithQuotaDeficit(Integer year) {
        return companyRepository.findCompaniesWithQuotaDeficit(year);
    }

    public Long countByIndustry(Long industryId) {
        return companyRepository.countByIndustryId(industryId);
    }

    public void updateQuotaHeld(Long companyId, BigDecimal quotaHeld) {
        companyRepository.findById(companyId).ifPresent(company -> {
            company.setQuotaHeld(quotaHeld);
            companyRepository.save(company);
        });
    }

    public void updateComplianceStatus(Long companyId, String complianceStatus) {
        companyRepository.findById(companyId).ifPresent(company -> {
            company.setComplianceStatus(complianceStatus);
            companyRepository.save(company);
        });
    }

    public void initIndustries() {
        if (industryRepository.count() == 0) {
            Industry power = new Industry();
            power.setCode("POWER");
            power.setName("电力");
            power.setDescription("发电企业");
            power.setStatus("ACTIVE");
            industryRepository.save(power);

            Industry steel = new Industry();
            steel.setCode("STEEL");
            steel.setName("钢铁");
            steel.setDescription("钢铁生产企业");
            steel.setStatus("ACTIVE");
            industryRepository.save(steel);

            Industry cement = new Industry();
            cement.setCode("CEMENT");
            cement.setName("水泥");
            cement.setDescription("水泥生产企业");
            cement.setStatus("ACTIVE");
            industryRepository.save(cement);

            Industry aluminum = new Industry();
            aluminum.setCode("ALUMINUM");
            aluminum.setName("电解铝");
            aluminum.setDescription("电解铝生产企业");
            aluminum.setStatus("ACTIVE");
            industryRepository.save(aluminum);

            Industry petrochemical = new Industry();
            petrochemical.setCode("PETROCHEMICAL");
            petrochemical.setName("石化");
            petrochemical.setDescription("石化生产企业");
            petrochemical.setStatus("ACTIVE");
            industryRepository.save(petrochemical);

            Industry chemical = new Industry();
            chemical.setCode("CHEMICAL");
            chemical.setName("化工");
            chemical.setDescription("化工生产企业");
            chemical.setStatus("ACTIVE");
            industryRepository.save(chemical);

            Industry paper = new Industry();
            paper.setCode("PAPER");
            paper.setName("造纸");
            paper.setDescription("造纸生产企业");
            paper.setStatus("ACTIVE");
            industryRepository.save(paper);

            Industry aviation = new Industry();
            aviation.setCode("AVIATION");
            aviation.setName("航空");
            aviation.setDescription("航空运输企业");
            aviation.setStatus("ACTIVE");
            industryRepository.save(aviation);
        }
    }

    public List<Industry> findAllIndustries() {
        return industryRepository.findAll();
    }

    public Optional<Industry> findIndustryByCode(String code) {
        return industryRepository.findByCode(code);
    }
}
