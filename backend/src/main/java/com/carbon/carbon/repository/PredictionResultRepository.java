package com.carbon.carbon.repository;

import com.carbon.carbon.entity.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {

    List<PredictionResult> findAllByOrderByPredictionTimeDesc();

    List<PredictionResult> findByPredictionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    Optional<PredictionResult> findTopByOrderByPredictionTimeDesc();

    @Query("SELECT p FROM PredictionResult p WHERE p.modelVersion = :version ORDER BY p.predictionTime DESC")
    List<PredictionResult> findByModelVersion(String version);
}
