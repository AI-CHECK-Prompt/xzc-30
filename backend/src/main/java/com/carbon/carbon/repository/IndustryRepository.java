package com.carbon.carbon.repository;

import com.carbon.carbon.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {

    Optional<Industry> findByCode(String code);

    List<Industry> findByStatus(String status);
}
