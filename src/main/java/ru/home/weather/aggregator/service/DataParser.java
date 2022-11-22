package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.home.weather.aggregator.domain.Indication;

import java.text.ParseException;
import java.util.List;

/**
 * @author Elena Demeneva
 */
public interface DataParser {
    List<Indication> parseForecastIndications(String data) throws JsonProcessingException, ParseException;

    Indication parseObservationIndication(String data) throws JsonProcessingException, ParseException;
}
