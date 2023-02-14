package ru.home.weather.aggregator.domain;


import lombok.Getter;
import lombok.ToString;

import java.time.temporal.ChronoUnit;

/**
 * @author Elena Demeneva
 */
@Getter
@ToString
public class PairForecastObservation {
    private int antiquity;
    private Indication forecast;
    private Indication observation;

    public PairForecastObservation(Indication forecast, Indication observation) {
        setForecast(forecast);
        this.observation = observation;
    }

    private void setForecast(Indication forecast) {
        this.forecast = forecast;
        antiquity = (int)ChronoUnit.DAYS.between(forecast.getDateIndicate(),forecast.getDateRequest());
     }

}