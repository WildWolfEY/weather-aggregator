package ru.home.weather.aggregator.domain;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Elena Demeneva
 */
@Component
@Scope(value = "prototype")
@Getter
public class Pair {
    private int days;
    private Indication forecast;
    private Indication observation;
    public Pair(Indication forecast, Indication observation){
           setForecast(forecast);
           this.observation = observation;
    }
    private void setForecast(Indication forecast) {
        this.forecast = forecast;
        days =Math.abs(Math.round((forecast.getDateIndicate().getEpochSecond() - forecast.getDateRequest().getEpochSecond())/(60*60*24)));
    }

}