package com.carbon.carbon.repository;

import com.carbon.carbon.entity.SignalTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalTrackingRepository extends JpaRepository<SignalTracking, Long> {

    List<SignalTracking> findBySignalId(Long signalId);

    @Query("SELECT st FROM SignalTracking st WHERE st.signalResult = :result")
    List<SignalTracking> findBySignalResult(String result);

    @Query("SELECT COUNT(st) FROM SignalTracking st WHERE st.signalResult = :result")
    Long countBySignalResult(String result);

    @Query("SELECT AVG(st.returnRate) FROM SignalTracking st WHERE st.signalResult = 'PROFIT'")
    Double getAverageProfitRate();

    @Query("SELECT AVG(st.returnRate) FROM SignalTracking st WHERE st.signalResult = 'LOSS'")
    Double getAverageLossRate();
}
