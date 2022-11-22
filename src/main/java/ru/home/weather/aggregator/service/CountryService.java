package ru.home.weather.aggregator.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Elena Demeneva
 */

@Service
public class CountryService {
    public Map<String, String> getCountries() {
        Map<String, String> countries = new TreeMap<>();
        String[] countriesAlpha2Code = Locale.getISOCountries();
        for (String country : countriesAlpha2Code) {
            Locale locale = new Locale("", country);
            countries.put(locale.getDisplayCountry(), locale.getCountry());
        }
        return countries;
    }
}
