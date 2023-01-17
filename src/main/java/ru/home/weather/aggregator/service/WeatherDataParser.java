package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.home.weather.aggregator.domain.Indication;

import java.text.ParseException;
import java.util.List;

/**
 * @author Elena Demeneva
 */
public interface WeatherDataParser<T, E> {
    List<Indication> parseForecastIndications(T data) throws JsonProcessingException, ParseException;

    Indication parseObservationIndication(E data) throws JsonProcessingException, ParseException;
}
