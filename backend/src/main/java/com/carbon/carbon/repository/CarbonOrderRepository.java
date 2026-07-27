package com.carbon.carbon.repository;

import com.carbon.carbon.entity.CarbonOrder;
import com.carbon.carbon.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarbonOrderRepository extends JpaRepository<CarbonOrder, Long> {

    List<CarbonOrder> findBySeller(Company seller);

    List<CarbonOrder> findByBuyer(Company buyer);

    List<CarbonOrder> findByOrderType(String orderType);

    List<CarbonOrder> findByTradingMode(String tradingMode);

    List<CarbonOrder> findByOrderStatus(String orderStatus);

    Optional<CarbonOrder> findByOrderNo(String orderNo);

    @Query("SELECT o FROM CarbonOrder o WHERE o.orderTime >= :startTime AND o.orderTime <= :endTime")
    List<CarbonOrder> findByOrderTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT o FROM CarbonOrder o WHERE (o.seller.id = :companyId OR o.buyer.id = :companyId) AND o.orderTime >= :startTime")
    List<CarbonOrder> findByCompanyIdAndOrderTimeAfter(@Param("companyId") Long companyId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT AVG(o.unitPrice) FROM CarbonOrder o WHERE o.orderStatus = 'COMPLETED' AND o.orderTime >= :startTime")
    Double calculateAveragePrice(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT SUM(o.totalAmount) FROM CarbonOrder o WHERE o.orderStatus = 'COMPLETED' AND o.orderTime >= :startTime")
    Double calculateTotalTradingVolume(@Param("startTime") LocalDateTime startTime);
}
