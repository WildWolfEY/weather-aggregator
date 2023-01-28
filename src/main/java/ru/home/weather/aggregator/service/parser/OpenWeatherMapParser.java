package ru.home.weather.aggregator.service.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.WebSiteRepository;
import ru.home.weather.aggregator.service.IntensityDeterminant;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Elena Demeneva
 */
@Service
@Log4j2
public class OpenWeatherMapParser implements WeatherDataParser<String, String> {

    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    IntensityDeterminant intensityDeterminant;

    private final URI url = URI.create("http://api.openweathermap.org/");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<City> parseCities(String responseBody) throws JsonProcessingException {
        log.debug("parseCities(String responseBody), параметр {}", responseBody);
        List<City> cities = new ArrayList<>();
        Locale currentLocale = Locale.getDefault();
        OpenWeatherMapParser.CityRaw[] citiesRaw = objectMapper.readValue(responseBody, CityRaw[].class);
        log.trace("citiesRaw: {}", citiesRaw);
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
            log.trace("adding city: {}", city);
        }
        log.debug("результат: {}", cities);
        return cities;
    }

    @Override
    public List<Indication> parseForecastIndications(String responseBody) throws JsonProcessingException {
        log.debug("parseForecastIndications(String responseBody), параметр {}", responseBody);
        IndicationsRaw indicationsRaw = objectMapper.readValue(responseBody, IndicationsRaw.class);
        log.trace("indicationsRaw {}", indicationsRaw);
        List<Indication> indications = new ArrayList<>();
        for (WeatherData weatherData : indicationsRaw.list) {
            Indication indication = createIndication(weatherData);
            indication.setForecast(true);
            indications.add(indication);
        }
        log.debug("результат {}", indications);
        return indications;
    }

    @Override
    public Indication parseObservationIndication(String responseBody) throws JsonProcessingException {
        log.debug("parseObservationIndication(String responseBody) параметр:{}", responseBody);
        WeatherData weatherData = objectMapper.readValue(responseBody, WeatherData.class);
        Indication indication = createIndication(weatherData);
        indication.setForecast(false);
        log.debug("результат:{}", indication);
        return indication;
    }

    private Indication createIndication(WeatherData weatherData) throws JsonProcessingException {
        log.debug("createIndication(WeatherData weatherData), параметр:{}", weatherData);
//        String conditions = new String();
//                Arrays.stream(weatherData.weather).forEach(x->x.description);
        String conditions =  Arrays.stream(weatherData.weather)
                .map(x->x.description)
                .collect(Collectors.joining(", "));
        Indication indication = Indication.builder()
                .dateRequest(Instant.now())
                .dateIndicate(Instant.ofEpochSecond(weatherData.dt))
                .temperature(weatherData.main.temp - 273.15f)
                .millimeters((weatherData.rain != null ? weatherData.rain.millimeters : 0) +
                        (weatherData.snow != null ? weatherData.snow.millimeters : 0))
                .intensity(intensityDeterminant.getIntensity(conditions))
                .webSite(webSiteRepository.findByHttp(url.toString())
                        .orElseGet(() -> saveWebSite(url)))
                .build();
        log.debug("результат: {}", indication);
        return indication;
    }

    private WebSite saveWebSite(URI url) {
        log.debug("saveWebSite(URI url) параметр:{}", url.toString());
        WebSite webSite = webSiteRepository.save(WebSite.builder()
                .http(url.toString())
                .title("OpenWeatherMap").build());
        log.info("результат: в БД сохранен новый webSite {}", webSite);
        return webSite;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
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
    @ToString
    public static class IndicationsRaw {
        WeatherData[] list;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class WeatherData {
        long dt;
        MainIndicator main;
        Weather[] weather;
        Rain rain;
        Snow snow;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class MainIndicator {
        float temp;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class Weather {
        String main;
        String description;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class Rain {
        @JsonProperty(value = "3h")
        float millimeters;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class Snow {
        @JsonProperty(value = "3h")
        float millimeters;
    }
}
