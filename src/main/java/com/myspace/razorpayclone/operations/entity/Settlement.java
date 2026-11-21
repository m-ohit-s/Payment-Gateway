package com.myspace.razorpayclone.operations.entity;

import com.myspace.razorpayclone.common.entity.BaseEntity;
import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settlement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID merchantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gross_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "gross_amount_currency", nullable = false))
    })
    private Money grossAmount;

    @Embedded()
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "refund_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "refund_amount_currency"))
    })
    private Money refundAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "fee_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "fee_amount_currency", nullable = false))
    })
    private Money feeAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "tax_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "tax_amount_currency", nullable = false))
    })
    private Money taxAmount; //GST on fee

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "net_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "net_amount_currency", nullable = false))
    })
    private Money netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SettlementStatus status =  SettlementStatus.INITIATED;

    @Column(nullable = false, length = 100)
    private String bankReference;

    private LocalDateTime processedAt;

    private String failureReason;

}
