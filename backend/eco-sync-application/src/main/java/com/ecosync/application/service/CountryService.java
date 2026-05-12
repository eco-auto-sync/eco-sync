package com.ecosync.application.service;

import com.ecosync.application.port.in.GetCountriesUseCase;
import com.ecosync.domain.subscription.Country;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryService implements GetCountriesUseCase {

    private static final List<Country> SUPPORTED_COUNTRIES = List.of(
            new Country("KR", "한국", "KRX", "🇰🇷"),
            new Country("US", "미국", "NYSE, NASDAQ", "🇺🇸"),
            new Country("JP", "일본", "JPX", "🇯🇵"),
            new Country("CN", "중국", "SHSE", "🇨🇳"),
            new Country("HK", "홍콩", "HKEX", "🇭🇰"),
            new Country("GB", "영국", "LSE", "🇬🇧"),
            new Country("DE", "독일", "XETRA", "🇩🇪")
    );

    @Override
    public List<Country> getCountries() {
        return SUPPORTED_COUNTRIES;
    }
}
