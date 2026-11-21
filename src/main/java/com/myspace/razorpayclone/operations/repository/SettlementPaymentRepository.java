package com.myspace.razorpayclone.operations.repository;

import com.myspace.razorpayclone.operations.entity.SettlementPayment;
import com.myspace.razorpayclone.operations.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
}
