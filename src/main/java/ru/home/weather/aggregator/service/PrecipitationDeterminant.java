package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.Precipitation;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class PrecipitationDeterminant {
    public Precipitation getPrecipitation(String condition) {
        log.debug("getIntensity(String condition), параметр: {}", condition);
        for (String item : light) {
            if (condition.toLowerCase().contains(item)) {
                return Precipitation.LIGHT;
            }
        }
        for (String item : heavy) {
            if (condition.toLowerCase().contains(item)) {
                return Precipitation.HEAVY;
            }
        }
        for (String item : normal) {
            if (condition.toLowerCase().contains(item)) {
                return Precipitation.NORMAL;
            }
        }
        return Precipitation.CLEAR;
    }

    private List<String> light = Arrays.asList("drizzle", "морось", "light", "небольшой");
    private List<String> heavy = Arrays.asList("сильный", "ливень", "снегопад", "heavy", "shower");
    private List<String> normal = Arrays.asList("дождь", "снег", "град", "rain", "snow", "hail");
}
