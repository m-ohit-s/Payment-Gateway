package com.myspace.razorpayclone.merchant.service;

import com.myspace.razorpayclone.merchant.dto.request.MerchantActivateRequest;
import com.myspace.razorpayclone.merchant.dto.response.MerchantActivateResponse;

import java.util.UUID;

public interface IMerchantService {
    MerchantActivateResponse activateMerchant(UUID merchantId, MerchantActivateRequest merchantActivateRequest);
}
