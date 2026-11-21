package com.myspace.razorpayclone.operations.settlement;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.operations.settlement.dto.BankTransferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferProcessor implements IBankTransferProcessor {

    @Override
    public BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc) {
        //Call the bank server API
        String registrationRef = "TXN_" + RandomizerUtil.randomBase64(12);
        log.debug("Bank transfer completed for settlementId : {}, registrationRef: {}", settlementId, registrationRef);
        return new  BankTransferResult(registrationRef);
    }
}
