package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.web.GisMeteoApiController;
import ru.home.weather.aggregator.web.OpenWeatherMapApiController;
import ru.home.weather.aggregator.web.YandexPogodaApiController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Set;

import static java.util.Calendar.*;

/**
 * @author Elena Demeneva
 */

@EnableScheduling
@Service
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
@Log4j2
public class IndicationCollector {
    @Autowired
    CityRepository cityRepository;
    @Autowired
    OpenWeatherMapApiController openWeatherMapApiController;
    @Autowired
    YandexPogodaApiController yandexPogodaApiController;
    @Autowired
    GisMeteoApiController gisMeteoApiController;
    @Autowired
    IndicationRepository indicationRepository;

    @Scheduled(cron = "${scheduler.interval.forecasts}")
    //@Scheduled (fixedDelay = 5*60*1000)
    public void saveForecasts() {
        log.debug("getForecasts()");
        for (City city : cityRepository.findAll()) {
            try {
                indicationRepository.saveAll(openWeatherMapApiController.getForecasts(city));
                indicationRepository.saveAll(yandexPogodaApiController.getForecasts(city));
                indicationRepository.saveAll(gisMeteoApiController.getForecasts(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос getForecasts");
            }
        }
    }

    @Scheduled(cron = "${scheduler.interval.observation}")
    public void saveObservations() {
        log.debug("getObservations()");
        for (City city : cityRepository.findAll()) {
            try {
                indicationRepository.save(openWeatherMapApiController.getObservation(city));
                indicationRepository.save(yandexPogodaApiController.getObservation(city));
                indicationRepository.save(gisMeteoApiController.getObservation(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос getObservations");
            }
        }
    }

    @Scheduled(cron = "${scheduler.interval.clear}")
    public void clear() {
        log.debug("clear()");
        LocalDate date = LocalDate.now().minusDays(15);
        try {
            indicationRepository.deleteByDateIndicateBefore(Instant.from(date));
        } catch (Exception exception) {
            log.warn("Не удалось выполнить очистку старых записей");
        }
    }
}
