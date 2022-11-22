package ru.home.weather.aggregator.web;

import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;

import java.util.List;

/**
 * @author Elena Demeneva
 */
public interface ApiController {
    public List<Indication> getForecasts(City city);

    public Indication getObservation(City city);
}
