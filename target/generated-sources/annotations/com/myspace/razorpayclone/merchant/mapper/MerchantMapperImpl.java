package com.myspace.razorpayclone.merchant.mapper;

import com.myspace.razorpayclone.common.enums.BusinessType;
import com.myspace.razorpayclone.common.enums.MerchantStatus;
import com.myspace.razorpayclone.merchant.dto.response.MerchantResponse;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-18T17:25:25+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.5 (Eclipse Adoptium)"
)
@Component
public class MerchantMapperImpl implements MerchantMapper {

    @Override
    public MerchantResponse toMerchantResponse(Merchant merchant) {
        if ( merchant == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String email = null;
        String businessName = null;
        BusinessType businessType = null;
        MerchantStatus merchantStatus = null;

        id = merchant.getId();
        name = merchant.getName();
        email = merchant.getEmail();
        businessName = merchant.getBusinessName();
        businessType = merchant.getBusinessType();
        merchantStatus = merchant.getMerchantStatus();

        MerchantResponse merchantResponse = new MerchantResponse( id, name, email, businessName, businessType, merchantStatus );

        return merchantResponse;
    }
}
