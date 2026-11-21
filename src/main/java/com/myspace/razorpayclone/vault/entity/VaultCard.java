package com.myspace.razorpayclone.vault.entity;

import com.myspace.razorpayclone.common.enums.CardBrand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Column(nullable = false, length = 6)
    private String bin;  //first 6 digits of card to recognize the card

    @Column(nullable = false)
    private byte[] encryptedPan;

    @Column(nullable = false)
    private byte[] encryptedDek;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardBrand brand; //brand of card VISA, MASTERCARD, AMEX

    @Column(nullable = false, length = 2)
    private Integer expiryMonth;
    @Column(nullable = false, length = 4)
    private Integer expiryYear;

    @Column(nullable = false, length = 100)
    private String cardHolderName;

    private LocalDateTime deletedAt;
}
