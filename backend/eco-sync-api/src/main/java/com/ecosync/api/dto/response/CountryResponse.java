package com.ecosync.api.dto.response;

import com.ecosync.domain.subscription.Country;

public record CountryResponse(String code, String name, String exchange, String flag) {

    public static CountryResponse from(Country country) {
        return new CountryResponse(country.code(), country.name(), country.exchange(), country.flag());
    }
}
