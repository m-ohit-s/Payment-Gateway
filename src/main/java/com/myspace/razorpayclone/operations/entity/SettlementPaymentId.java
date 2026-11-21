package com.myspace.razorpayclone.operations.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SettlementPaymentId {
    private UUID settlementId;
    private UUID paymentId;
}
