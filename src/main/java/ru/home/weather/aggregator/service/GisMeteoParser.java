package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.home.weather.aggregator.domain.Indication;

import java.text.ParseException;
import java.util.List;

/**
 * @author Elena Demeneva
 */
public class GisMeteoParser implements DataParser<String, String> {
    @Override
    public List<Indication> parseForecastIndications(String data) throws JsonProcessingException {
        return null;
    }

    @Override
    public Indication parseObservationIndication(String data) throws JsonProcessingException, ParseException {
        return null;
    }
}
