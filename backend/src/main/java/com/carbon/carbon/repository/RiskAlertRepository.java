package com.carbon.carbon.repository;

import com.carbon.carbon.entity.RiskAlert;
import com.carbon.carbon.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    List<RiskAlert> findByCompany(Company company);

    List<RiskAlert> findByAlertType(String alertType);

    List<RiskAlert> findByAlertLevel(String alertLevel);

    List<RiskAlert> findByAlertStatus(String alertStatus);

    @Query("SELECT r FROM RiskAlert r WHERE r.alertStatus = 'PENDING' ORDER BY r.alertTime DESC")
    List<RiskAlert> findPendingAlerts();

    @Query("SELECT r FROM RiskAlert r WHERE r.alertTime >= :startTime AND r.alertTime <= :endTime")
    List<RiskAlert> findByAlertTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(r) FROM RiskAlert r WHERE r.alertStatus = 'PENDING'")
    Long countPendingAlerts();
}
