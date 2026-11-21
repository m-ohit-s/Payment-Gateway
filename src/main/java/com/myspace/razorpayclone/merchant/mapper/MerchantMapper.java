package com.myspace.razorpayclone.merchant.mapper;

import com.myspace.razorpayclone.merchant.dto.response.MerchantResponse;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MerchantMapper {
    MerchantResponse toMerchantResponse(Merchant merchant);
}
