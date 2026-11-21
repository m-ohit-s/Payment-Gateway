package com.myspace.razorpayclone.payment.entity;

import com.myspace.razorpayclone.common.entity.BaseEntity;
import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "refund")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Refund extends BaseEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RefundStatus status =  RefundStatus.PENDING;

    @Column(nullable = false)
    private String bankReference;

    private String errorCode;
    private String errorDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    private LocalDateTime processedAt;
}
