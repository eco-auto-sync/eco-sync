package com.ecosync.api.controller;

import com.ecosync.api.dto.response.CountryResponse;
import com.ecosync.api.swagger.CountryApi;
import com.ecosync.application.port.in.GetCountriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController implements CountryApi {

    private final GetCountriesUseCase getCountriesUseCase;

    @Override
    @GetMapping
    public List<CountryResponse> getCountries() {
        return getCountriesUseCase.getCountries().stream()
                .map(CountryResponse::from)
                .toList();
    }
}
