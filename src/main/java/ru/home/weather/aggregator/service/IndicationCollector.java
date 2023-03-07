package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.StatisticRepository;
import ru.home.weather.aggregator.service.math.ObservationAgregator;
import ru.home.weather.aggregator.service.math.StatisticCalculator;
import ru.home.weather.aggregator.web.GisMeteoApiController;
import ru.home.weather.aggregator.web.OpenWeatherMapApiController;
import ru.home.weather.aggregator.web.YandexPogodaApiController;

import java.time.LocalDate;

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
    @Autowired
    StatisticCalculator statisticCalculator;
    @Autowired
    StatisticRepository statisticRepository;
    @Autowired
    ObservationAgregator observationAgregator;

    @Scheduled(cron = "${scheduler.interval.forecasts}")
    public void saveForecasts() {
        log.debug("saveForecasts()");
        for (City city : cityRepository.findAll()) {
            try {
                indicationRepository.saveAll(openWeatherMapApiController.getForecasts(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос saveForecasts для openWeatherMap");
            }
            try {
                indicationRepository.saveAll(yandexPogodaApiController.getForecasts(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос saveForecasts для yandexPogoda");
            }
            try {
                indicationRepository.saveAll(gisMeteoApiController.getForecasts(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос saveForecasts для gisMeteo");
            }
        }
    }

    @Scheduled(cron = "${scheduler.interval.observation}")
    public void saveObservations() {
        log.debug("getObservations()");
        for (City city : cityRepository.findAll()) {
            try {
                indicationRepository.save(openWeatherMapApiController.getObservation(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос saveObservations для WeatherMap", exception);
            }
            try {
                indicationRepository.save(yandexPogodaApiController.getObservation(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос saveObservations для yandexPogoda");
            }
            try {
                indicationRepository.save(gisMeteoApiController.getObservation(city));
            } catch (Exception exception) {
                log.warn("Не удалось корректно выполнить запрос saveObservations для gisMeteo");
            }
        }
    }

    @Scheduled(cron = "${scheduler.interval.statistic}")
    public void saveStatistic() {
        log.debug("getStatistic()");
        try {
            LocalDate startPeriod = LocalDate.now().minusDays(10);
            LocalDate endPeriod = LocalDate.now().minusDays(1);
            statisticRepository.saveAll(statisticCalculator.calculateStatistic(startPeriod, endPeriod));

        } catch (Exception exception) {
            log.warn("Не удалось посчитать статистику");
        }
    }

    @Scheduled(cron = "${scheduler.interval.avg.observation}")
    public void averageObservations() {
        LocalDate dateStart = LocalDate.now().minusDays(1);
        LocalDate dateEnd = LocalDate.now();
        log.debug("averageObservations()");
        try {
            indicationRepository.saveAll(observationAgregator.getAverageObservations(dateStart, dateEnd));
        } catch (Exception exception) {
            log.warn("Не удалось посчитать среднее значение по наблюдениям");
        }
    }
}
