package com.carbon.carbon.repository;

import com.carbon.carbon.entity.Quota;
import com.carbon.carbon.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaRepository extends JpaRepository<Quota, Long> {

    List<Quota> findByCompany(Company company);

    List<Quota> findByYear(Integer year);

    List<Quota> findByCompanyAndYear(Company company, Integer year);

    Optional<Quota> findByCompanyIdAndYear(Long companyId, Integer year);

    List<Quota> findByComplianceStatus(String complianceStatus);

    @Query("SELECT q FROM Quota q WHERE q.complianceStatus = 'NON_COMPLIANT' AND q.year = :year")
    List<Quota> findNonCompliantByYear(@Param("year") Integer year);

    @Query("SELECT SUM(q.currentBalance) FROM Quota q WHERE q.year = :year")
    Double sumCurrentBalanceByYear(@Param("year") Integer year);
}
