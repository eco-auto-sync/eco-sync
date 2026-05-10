package com.ecosync.api.controller;

import com.ecosync.api.dto.response.CountryResponse;
import com.ecosync.application.port.in.GetCountriesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Countries", description = "지원 국가 목록")
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final GetCountriesUseCase getCountriesUseCase;

    @Operation(summary = "지원 국가 목록 조회")
    @GetMapping
    public List<CountryResponse> getCountries() {
        return getCountriesUseCase.getCountries().stream()
                .map(CountryResponse::from)
                .toList();
    }
}
