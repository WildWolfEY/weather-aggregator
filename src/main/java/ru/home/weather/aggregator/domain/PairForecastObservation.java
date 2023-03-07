package ru.home.weather.aggregator.domain;

import lombok.Getter;
import lombok.ToString;

import java.time.temporal.ChronoUnit;

@Getter
@ToString
public class PairForecastObservation {
    private final int antiquity;
    private final Indication forecast;
    private final Indication observation;

    public PairForecastObservation(Indication forecast, Indication observation) {
        this.observation = observation;
        this.forecast = forecast;
        antiquity = Math.abs((int) ChronoUnit.DAYS.between(forecast.getDateIndicate(), forecast.getDateRequest()));
    }
}
