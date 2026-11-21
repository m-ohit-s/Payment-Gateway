package com.myspace.razorpayclone.operations.repository;

import com.myspace.razorpayclone.common.enums.SettlementStatus;
import com.myspace.razorpayclone.operations.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByStatus(SettlementStatus status);

    List<Settlement> findByStatusAndCreatedAtBefore(SettlementStatus settlementStatus, LocalDateTime globalWindow);
}
