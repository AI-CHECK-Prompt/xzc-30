package com.carbon.carbon.repository;

import com.carbon.carbon.entity.EmissionData;
import com.carbon.carbon.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmissionDataRepository extends JpaRepository<EmissionData, Long> {

    List<EmissionData> findByCompany(Company company);

    List<EmissionData> findByYear(Integer year);

    List<EmissionData> findByCompanyAndYear(Company company, Integer year);

    List<EmissionData> findByVerificationStatus(String verificationStatus);

    @Query("SELECT e FROM EmissionData e WHERE e.company.id = :companyId AND e.year = :year")
    Optional<EmissionData> findByCompanyIdAndYear(@Param("companyId") Long companyId, @Param("year") Integer year);

    @Query("SELECT SUM(e.totalEmissions) FROM EmissionData e WHERE e.year = :year")
    Double sumTotalEmissionsByYear(@Param("year") Integer year);

    @Query("SELECT e FROM EmissionData e WHERE e.verificationStatus = 'PENDING'")
    List<EmissionData> findPendingVerification();
}
