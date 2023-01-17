package ru.home.weather.aggregator.web;

import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;

import java.io.IOException;
import java.util.List;

/**
 * @author Elena Demeneva
 */
public interface WeatherApiController {
    public List<Indication> getForecasts(City city);

    public Indication getObservation(City city) throws IOException, InterruptedException;
}
