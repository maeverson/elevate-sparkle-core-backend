package com.elevate.sparkle.adapter.out.persistence.repository;

import com.elevate.sparkle.adapter.out.persistence.entity.OrderJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Order
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findByUserId(String userId);

    List<OrderJpaEntity> findByStatus(String status);

    List<OrderJpaEntity> findAllBy(Pageable pageable);
}
