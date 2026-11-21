package com.myspace.razorpayclone.operations.settlement;

import com.myspace.razorpayclone.common.dto.SettlementBankDetails;
import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.EventAggregateType;
import com.myspace.razorpayclone.common.enums.SettlementStatus;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.merchant.api.IMerchantLookupService;
import com.myspace.razorpayclone.operations.entity.Settlement;
import com.myspace.razorpayclone.operations.entity.SettlementPayment;
import com.myspace.razorpayclone.operations.entity.SettlementPaymentId;
import com.myspace.razorpayclone.operations.repository.SettlementPaymentRepository;
import com.myspace.razorpayclone.operations.repository.SettlementRepository;
import com.myspace.razorpayclone.operations.settlement.dto.BankTransferResult;
import com.myspace.razorpayclone.payment.entity.Payment;
import com.myspace.razorpayclone.payment.outbox.OutboxEventPublisher;
import com.myspace.razorpayclone.payment.service.impl.PaymentLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementTransactionExecutor {
    private final PaymentLookupService paymentLookupService;
    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final IMerchantLookupService merchantLookupService;
    private final IBankTransferProcessor bankTransferProcessor;
    private static final double FEE_RATE = 0.02;
    private static final double GST_RATE = 0.18;
    //TODO: publisher inside its own db
    private final OutboxEventPublisher outboxEventPublisher;

    public void processForMerchant(UUID merchantId, LocalDate settlementDate) {
        List<Payment> unsettledPayments = paymentLookupService.findUnsettledCapturedPayments(merchantId);
        if (unsettledPayments.isEmpty()) return;

        log.info("Processing unsettled payments for merchant : {}, date: {}", merchantId, settlementDate);
        Money gross = unsettledPayments.stream()
                .map(Payment::getAmount)
                .reduce(Money::add)
                .orElseThrow();

        int fee = Math.toIntExact(Math.round(gross.getAmountUnits() * FEE_RATE));
        int gst = Math.toIntExact(Math.round(gross.getAmountUnits() * GST_RATE));
        Money feeMoney = Money.of(fee, gross.getCurrency());
        Money gstMoney = Money.of(gst, gross.getCurrency());
        Money netMoney = gross
                .subtract(feeMoney)
                .subtract(gstMoney);
        Settlement settlement = Settlement.builder()
                .merchantId(merchantId)
                .grossAmount(gross)
                .feeAmount(feeMoney)
                .taxAmount(gstMoney)
                .netAmount(netMoney)
                .bankReference("Random Bank Reference")
                .status(SettlementStatus.INITIATED)
                .build();
        settlement = settlementRepository.save(settlement);
        List<SettlementPayment> links = new ArrayList<>();
        try {
            for (Payment payment : unsettledPayments) {
                links.add(SettlementPayment.builder()
                        .id(new SettlementPaymentId(settlement.getId(), payment.getId()))
                        .settlement(settlement)
                        .build()
                );
            }
            settlementPaymentRepository.saveAll(links);

            SettlementBankDetails bankDetails = merchantLookupService.getSettlementBankDetails(merchantId);
            // call the bank transfer service to transfer amount to merchant's bank
            BankTransferResult result = bankTransferProcessor.initiate(settlement.getId(), merchantId, netMoney, bankDetails.accountNumber(), bankDetails.ifsc());

            settlement.setStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(result.registrationRef());
            settlementRepository.save(settlement);
        } catch (Exception e) {
            log.error("Settlement failed for settlementId : {}, error : {}, date: {}", settlement.getId(), e.getMessage(), settlementDate);
            settlement.setStatus(SettlementStatus.FAILED);
            settlementRepository.save(settlement);
        }
    }


    public void resolveTransfer(UUID settlementId, String errorCode, String errorDescription) {
        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow(
                () -> new ResourceNotFoundException("SETTLEMENT", settlementId)
        );
        if (!settlement.getStatus().equals(SettlementStatus.TRANSFER_PENDING)) {
            log.info("Settlement resolved for settlement : {}", settlement.getId());
            return;
        }
        if (errorCode == null) {
            settlement.setStatus(SettlementStatus.PROCESSED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);
            log.info("Settlement processed for settlement : {}", settlement.getId());
            outboxEventPublisher.publish(
                    EventAggregateType.SETTLEMENT,
                    settlementId,
                  "SETTLEMENT_PROCESSED",
                    Map.of(
                            "settlementId", settlementId,
                            "merchantId", settlement.getMerchantId(),
                            "amount", settlement.getNetAmount().getAmountUnits(),
                           "status", settlement.getStatus().name(),
                           "currency", settlement.getNetAmount().getCurrency()
                    )
            );
        } else {
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(errorCode + " : " + errorDescription);
            settlementRepository.save(settlement);
            log.warn("Settlement failed for settlement : {}", settlement.getId());
            outboxEventPublisher.publish(
                    EventAggregateType.SETTLEMENT,
                    settlementId,
                    "SETTLEMENT_FAILED",
                    Map.of(
                            "settlementId", settlementId,
                            "merchantId", settlement.getMerchantId(),
                            "amount", settlement.getNetAmount().getAmountUnits(),
                            "status", settlement.getStatus().name(),
                            "currency", settlement.getNetAmount().getCurrency()
                    )
            );
        }

    }

}
