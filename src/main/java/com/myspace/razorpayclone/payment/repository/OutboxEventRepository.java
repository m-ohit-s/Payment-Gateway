package com.myspace.razorpayclone.payment.repository;

import com.myspace.razorpayclone.common.enums.OutboxStatus;
import com.myspace.razorpayclone.payment.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
