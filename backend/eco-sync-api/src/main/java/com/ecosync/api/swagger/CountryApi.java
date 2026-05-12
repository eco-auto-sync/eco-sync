package com.ecosync.api.swagger;

import com.ecosync.api.dto.response.CountryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Countries", description = "지원 국가 목록")
public interface CountryApi {

    @Operation(summary = "지원 국가 목록 조회")
    List<CountryResponse> getCountries();
}
