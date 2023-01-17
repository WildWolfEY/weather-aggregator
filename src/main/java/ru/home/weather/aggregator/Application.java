package ru.home.weather.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Elena Demeneva
 */
@SpringBootApplication
public class Application {
    public static void main (String[] args){
        SpringApplication.run(Application.class, args);
    }
    @Bean
    public Map<String, String>  countries() {
        Map<String, String> countries = new TreeMap<>();
            String[] countriesAlpha2Code = Locale.getISOCountries();
            for (String country : countriesAlpha2Code) {
                Locale locale = new Locale("", country);
                countries.put(locale.getDisplayCountry(), locale.getCountry());
            }
            return countries;
    }
}
