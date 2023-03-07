package ru.home.weather.aggregator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class WeatherAgregatorConfiguration {
    @Bean
    public Map<String, String> countries() {
        Map<String, String> countries = new TreeMap<>();
        String[] countriesAlpha2Code = Locale.getISOCountries();
        for (String country : countriesAlpha2Code) {
            Locale locale = new Locale("", country);
            countries.put(locale.getDisplayCountry(), locale.getCountry());
        }
        return countries;
    }

    @Bean
    public Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }
}
