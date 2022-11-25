package ru.home.weather.aggregator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;
import ru.home.weather.aggregator.web.OpenWeatherMapApiController;
import ru.home.weather.aggregator.web.YandexPogodaApiController;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

import static java.util.Calendar.*;

/**
 * @author Elena Demeneva
 */

@EnableScheduling
@Service
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class IndicationCollector {
    @Autowired
    CityRepository cityRepository;
    @Autowired
    OpenWeatherMapApiController openWeatherMapApiController;
    @Autowired
    YandexPogodaApiController yandexPogodaApiController;
    @Autowired
    IndicationRepository indicationRepository;

    @Scheduled(cron = "${scheduler.interval.forecasts}")
    //@Scheduled (fixedDelay = 5*60*1000)
    public void getForecasts() {
        Iterable<City> cities = cityRepository.findAll();
        for (City city : cities) {
            try {
                indicationRepository.saveAll(openWeatherMapApiController.getForecasts(city));
                indicationRepository.saveAll(yandexPogodaApiController.getForecasts(city));
            } catch (HttpClientErrorException exception) {
                System.err.println("Status: " + exception.getStatusCode().toString() + "; " + exception.getMessage());
            }
        }

        System.out.println(new Date() +" - getForecasts()");
    }

    @Scheduled(cron = "${scheduler.interval.observation}")
    public void getObservations() {
        Iterable<City> cities = cityRepository.findAll();
        for (City city : cities) {
            try {
                indicationRepository.save(openWeatherMapApiController.getObservation(city));
            } catch (HttpClientErrorException exception) {
                System.err.println("Status: " + exception.getStatusCode().toString() + "; " + exception.getMessage());
            }
        }
        System.out.println(new Date() +" - getObservations()");
    }

    @Scheduled(cron = "${scheduler.interval.clear}")
    public void clear() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(DATE, -1);
        Instant date = Instant.ofEpochMilli(calendar.getTimeInMillis());
        indicationRepository.deleteByDateIndicateBefore(date);
        System.out.println(new Date() +" - clear()");
    }
}
