package com.myspace.razorpayclone.merchant.dto.request;

public record MerchantActivateRequest(
        String gstId,
        String panId,
        String settlementBankAccount,
        String settlementBankIfsc,
        String settlementBankAccountHolderName
) {
}
