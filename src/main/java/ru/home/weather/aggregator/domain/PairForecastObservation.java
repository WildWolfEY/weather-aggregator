package ru.home.weather.aggregator.domain;


import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Elena Demeneva
 */
@Getter
public class PairForecastObservation {
    private int prescription;
    private Indication forecast;
    private Indication observation;

    public PairForecastObservation(Indication forecast, Indication observation) {
        setForecast(forecast);
        this.observation = observation;
    }

    private void setForecast(Indication forecast) {
        this.forecast = forecast;
        prescription = Math.abs(Math.round((forecast.getDateIndicate().getEpochSecond() - forecast.getDateRequest().getEpochSecond()) / (60 * 60 * 24)));
    }

}