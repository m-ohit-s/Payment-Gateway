package com.myspace.razorpayclone.operations.settlement;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.operations.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface IBankTransferProcessor {
    BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc);
}
