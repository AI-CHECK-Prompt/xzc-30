package com.carbon.carbon.repository;

import com.carbon.carbon.entity.ExternalFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExternalFactorRepository extends JpaRepository<ExternalFactor, Long> {

    List<ExternalFactor> findByFactorDate(LocalDate factorDate);

    List<ExternalFactor> findByFactorType(String factorType);

    List<ExternalFactor> findByFactorDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT f.factorType FROM ExternalFactor f")
    List<String> findDistinctFactorTypes();

    @Query("SELECT f FROM ExternalFactor f WHERE f.factorType = :type ORDER BY f.factorDate DESC")
    List<ExternalFactor> findLatestByType(String type);
}
