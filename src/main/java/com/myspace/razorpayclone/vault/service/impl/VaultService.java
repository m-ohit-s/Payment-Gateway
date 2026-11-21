package com.myspace.razorpayclone.vault.service.impl;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.CardBrand;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.payment.processor.PaymentProcessorRouter;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorRequest;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;
import com.myspace.razorpayclone.vault.config.VaultEncryptionConfig;
import com.myspace.razorpayclone.vault.dto.request.TokenizeRequest;
import com.myspace.razorpayclone.vault.dto.response.TokenizeResponse;
import com.myspace.razorpayclone.vault.entity.CardToken;
import com.myspace.razorpayclone.vault.entity.VaultCard;
import com.myspace.razorpayclone.vault.repository.CardTokenRepository;
import com.myspace.razorpayclone.vault.repository.VaultCardRepository;
import com.myspace.razorpayclone.vault.service.IVaultService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VaultService implements IVaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse tokenize(UUID merchantId, TokenizeRequest tokenizeRequest) {
        String lastFour = tokenizeRequest.pan().substring(tokenizeRequest.pan().length() - 4);
        String bin = tokenizeRequest.pan().substring(0, 6);
        CardBrand cardBrand = detectBrand(tokenizeRequest.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek)
                .encrypt(tokenizeRequest.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encryptedDec = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .lastFour(lastFour)
                .bin(bin)
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDec)
                .brand(cardBrand)
                .expiryMonth(tokenizeRequest.expiryMonth())
                .expiryYear(tokenizeRequest.expiryYear())
                .cardHolderName(tokenizeRequest.cardHolderName())
                .build();
        vaultCard = vaultCardRepository.save(vaultCard);

        String randomToken = RandomizerUtil.randomBase64(32);

        CardToken cardToken = CardToken.builder()
                .token(randomToken)
                .vaultCard(vaultCard)
                .merchantId(merchantId)
                .customerId(tokenizeRequest.customerId())
                .build();

        cardTokenRepository.save(cardToken);

        return new TokenizeResponse(
                randomToken,
                lastFour,
                cardBrand,
                tokenizeRequest.expiryMonth(),
                tokenizeRequest.expiryYear()
        );
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CARD_TOKEN_NULL", "Invalid card token " + token));
        VaultCard vaultCard = cardToken.getVaultCard();
        byte[] panBytes = null;

        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEncryptionConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());
            String pan = new String(panBytes, StandardCharsets.UTF_8);
            Integer expiryMonth = vaultCard.getExpiryMonth();
            Integer expiryYear = vaultCard.getExpiryYear();
            String expiry = expiryMonth + "/" + expiryYear;

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card(
                    paymentId,
                    pan,
                    expiry,
                    amount,
                    methodDetails
            );
            return paymentProcessorRouter.charge(paymentProcessorRequest);
        } catch (Exception e) {
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if (panBytes != null) {
                Arrays.fill(panBytes, (byte) 0);
            }
        }
    }

    private CardBrand detectBrand(String pan) {
        if (pan.startsWith("4")) return CardBrand.VISA;
        if (pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if (pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
