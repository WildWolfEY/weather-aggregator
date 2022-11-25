package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * @author Elena Demeneva
 */
@Service
public class OpenWeatherMapParser implements DataParser<String, String> {

    @Autowired
    WebSiteRepository webSiteRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    public List<City> parseCities(String responseBody) throws JsonProcessingException {
        List<City> cities = new ArrayList<>();
        Locale currentLocale = Locale.getDefault();
        OpenWeatherMapParser.CityRaw[] citiesRaw = objectMapper.readValue(responseBody, CityRaw[].class);
        for (CityRaw rawItem : citiesRaw) {
            Set<String> localNames = new HashSet<>();
            localNames.add(rawItem.getName());
            if (rawItem.local_names != null) {
                localNames.add(rawItem.getLocal_names().get(currentLocale.getLanguage()));
            }
            City city = City.builder()
                    .names(localNames)
                    .area(rawItem.getState())
                    .country(rawItem.getCountry())
                    .latitude(rawItem.getLat())
                    .longitude(rawItem.getLon())
                    .build();
            city.toJson();
            cities.add(city);
        }
        return cities;
    }

    @Override
    public List<Indication> parseForecastIndications(String responseBody) throws JsonProcessingException {
        IndicationsRaw indicationsRaw = objectMapper.readValue(responseBody, IndicationsRaw.class);
        List<Indication> indications = new ArrayList<>();
        for (WeatherData weatherData : indicationsRaw.list) {
            Indication indication = createIndication(weatherData);
            indication.setForecast(true);
            indications.add(indication);
        }
        return indications;
    }

    @Override
    public Indication parseObservationIndication(String responseBody) throws JsonProcessingException {
        WeatherData weatherData = objectMapper.readValue(responseBody, WeatherData.class);
        Indication indication = createIndication(weatherData);
        indication.setForecast(false);
        return indication;
    }

    private Indication createIndication(WeatherData weatherData) throws JsonProcessingException {

        return Indication.builder()
                .dateRequest(Instant.now())
                .dateIndicate(Instant.ofEpochSecond(weatherData.dt))
                .temperature(weatherData.main.temp - 273.15f)
                .millimeters((weatherData.rain != null ? weatherData.rain.millimeters : 0) +
                        (weatherData.snow != null ? weatherData.snow.millimeters : 0))
                .webSite(getWebSite())
                .build();
    }

    private WebSite getWebSite() {
        URI url = URI.create("http://api.openweathermap.org/");
        return webSiteRepository.findByHttp(url.toString())
                .orElseGet(() -> webSiteRepository.save(WebSite.builder()
                        .http(url.toString())
                        .title("OpenWeatherMap").build()));
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CityRaw {
        String name;
        Map<String, String> local_names;
        float lat;
        float lon;
        String country;
        String state;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndicationsRaw {
        WeatherData[] list;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WeatherData {
        long dt;
        MainIndicator main;
        Rain rain;
        Snow snow;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MainIndicator {
        float temp;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Rain {
        @JsonProperty(value = "3h")
        float millimeters;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Snow {
        @JsonProperty(value = "3h")
        float millimeters;
    }
}
