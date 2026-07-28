package com.carbon.carbon.repository;

import com.carbon.carbon.entity.TradingSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {

    List<TradingSignal> findAllByOrderBySignalTimeDesc();

    List<TradingSignal> findBySignalTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    Optional<TradingSignal> findTopByOrderBySignalTimeDesc();

    List<TradingSignal> findBySignalType(String signalType);

    @Query("SELECT s FROM TradingSignal s WHERE s.modelVersion = :version ORDER BY s.signalTime DESC")
    List<TradingSignal> findByModelVersion(String version);

    @Query("SELECT COUNT(s) FROM TradingSignal s WHERE s.signalType = :signalType")
    Long countBySignalType(String signalType);
}
