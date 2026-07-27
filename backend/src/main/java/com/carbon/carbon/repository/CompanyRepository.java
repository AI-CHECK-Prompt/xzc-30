package com.carbon.carbon.repository;

import com.carbon.carbon.entity.Company;
import com.carbon.carbon.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByIndustry(Industry industry);

    List<Company> findByRegion(String region);

    List<Company> findByComplianceStatus(String complianceStatus);

    @Query("SELECT c FROM Company c WHERE c.industry.id = :industryId")
    List<Company> findByIndustryId(@Param("industryId") Long industryId);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.industry.id = :industryId")
    Long countByIndustryId(@Param("industryId") Long industryId);

    @Query("SELECT c FROM Company c WHERE c.quotaHeld < c.totalEmissions AND c.year = :year")
    List<Company> findCompaniesWithQuotaDeficit(@Param("year") Integer year);
}
