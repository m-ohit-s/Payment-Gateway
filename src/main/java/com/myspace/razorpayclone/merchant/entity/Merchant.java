package com.myspace.razorpayclone.merchant.entity;

import com.myspace.razorpayclone.common.entity.BaseEntity;
import com.myspace.razorpayclone.common.enums.BusinessType;
import com.myspace.razorpayclone.common.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "merchant",
        indexes = {
                @Index(name = "idx_merchant_status", columnList = "merchant_status")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Merchant extends BaseEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(length = 20)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;
    @Column(length = 100)
    private String businessName;
    @Column(length = 200)
    private String websiteUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MerchantStatus merchantStatus = MerchantStatus.PENDING_KYC;

    @Column(length = 20)
    private String gstId;
    @Column(length = 20)
    private String panId;

    @Column(length = 200)
    private String settlementBankAccount;
    @Column(length = 20)
    private String settlementBankIfsc;
    @Column(length = 200)
    private String settlementBankAccountHolderName;

}
