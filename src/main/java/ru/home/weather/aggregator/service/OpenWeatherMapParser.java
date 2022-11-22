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
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Elena Demeneva
 */
@Service
public class OpenWeatherMapParser implements DataParser {

    @Autowired
    WebSiteRepository webSiteRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    public List<City> parseCities(String responseBody) throws JsonProcessingException {
        List<City> cities = new ArrayList<>();
        Locale currentLocale = Locale.getDefault();
        OpenWeatherMapParser.CityRaw[] citiesRaw = objectMapper.readValue(responseBody, CityRaw[].class);
        for (CityRaw rawItem : citiesRaw) {
            City city = new City();
            if (rawItem.local_names != null) {
                city.getNames().add(rawItem.getLocal_names().get(currentLocale.getLanguage()));
            }
            city.getNames().add(rawItem.getName());
            city.setLatitude(rawItem.getLat());
            city.setLongitude(rawItem.getLon());
            city.setArea(rawItem.getState());
            city.setCountry(rawItem.getCountry());
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
                .webSite(webSiteRepository.findByHttp("openweathermap.org").get(0))
                .build();
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
