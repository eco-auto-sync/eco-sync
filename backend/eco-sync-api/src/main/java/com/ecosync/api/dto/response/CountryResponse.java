package com.ecosync.api.dto.response;

import com.ecosync.domain.subscription.Country;
import io.swagger.v3.oas.annotations.media.Schema;

public record CountryResponse(
        @Schema(description = "국가 코드", example = "KR") String code,
        @Schema(description = "국가명", example = "한국") String name,
        @Schema(description = "거래소", example = "KRX") String exchange,
        @Schema(description = "국기 이모지", example = "🇰🇷") String flag
) {

    public static CountryResponse from(Country country) {
        return new CountryResponse(country.code(), country.name(), country.exchange(), country.flag());
    }
}
