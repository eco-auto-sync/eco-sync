package com.ecosync.application.port.in;

import com.ecosync.domain.subscription.Country;

import java.util.List;

public interface GetCountriesUseCase {
    List<Country> getCountries();
}
