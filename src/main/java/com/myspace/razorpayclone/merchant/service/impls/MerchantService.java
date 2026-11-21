package com.myspace.razorpayclone.merchant.service.impls;

import com.myspace.razorpayclone.common.enums.MerchantStatus;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.merchant.dto.request.MerchantActivateRequest;
import com.myspace.razorpayclone.merchant.dto.response.MerchantActivateResponse;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import com.myspace.razorpayclone.merchant.repository.MerchantRepository;
import com.myspace.razorpayclone.merchant.service.IMerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService implements IMerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    public MerchantActivateResponse activateMerchant(UUID merchantId, MerchantActivateRequest merchantActivateRequest) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(
                () -> new ResourceNotFoundException("MERCHANT", merchantId)
        );
        merchant.setPanId(merchantActivateRequest.panId());
        merchant.setGstId(merchantActivateRequest.gstId());
        merchant.setSettlementBankAccount(merchantActivateRequest.settlementBankAccount());
        merchant.setSettlementBankAccountHolderName(merchantActivateRequest.settlementBankAccountHolderName());
        merchant.setSettlementBankIfsc(merchantActivateRequest.settlementBankIfsc());
        merchant.setMerchantStatus(MerchantStatus.ACTIVE);

        merchant = merchantRepository.save(merchant);
        return new MerchantActivateResponse(merchant.getMerchantStatus());
    }
}
