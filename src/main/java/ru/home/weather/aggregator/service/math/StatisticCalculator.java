package ru.home.weather.aggregator.service.math;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.*;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.StatisticRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class StatisticCalculator {

    @Autowired
    IndicationRepository indicationRepository;
    @Autowired
    StatisticRepository statisticRepository;
    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    StandartDeviationCalculator standartDeviationCalculator;

    public List<Statistic> calculateStatistic(LocalDate dateStart, LocalDate dateEnd) throws IllegalArgumentException {
        log.debug("calculateStatistic(LocalDate dateStart, LocalDate dateEnd), параметры:{},{}", dateStart, dateEnd);
        List<Statistic> statistics = new ArrayList<>();
        List<Statistic> existStatistics = statisticRepository.findByStartPeriodAndEndPeriod(dateStart, dateEnd);
        Map<Key, List<PairForecastObservation>> groupingPairs = getGroupingForecastsAndObservationsForPeriod(
                dateStart.atStartOfDay().toInstant(ZoneOffset.UTC),
                dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC));
        for (Map.Entry<Key, List<PairForecastObservation>> groupForecastObservation : groupingPairs.entrySet()) {
            Statistic statistic = calculateStatistic(groupForecastObservation.getKey(), groupForecastObservation.getValue());
            statistic.setStartPeriod(dateStart);
            statistic.setEndPeriod(dateEnd);
            if (existStatistics.contains(statistic)) {
                statistic.setId(existStatistics.get(existStatistics.indexOf(statistic)).getId());
            }
            statistics.add(statistic);
            log.debug("adding: {}", statistic);
        }
        return statistics;
    }

    private Map<Key, List<PairForecastObservation>> getGroupingForecastsAndObservationsForPeriod(Instant dateStart, Instant dateEnd) {
        log.debug("getGroupingForecastsAndObservationsForPeriod(Instant dateStart, Instant dateEnd), параметры:{},{}", dateStart, dateEnd);
        Map<Key, List<PairForecastObservation>> groupingPairs = new HashMap<>();
        for (WebSite webSite : webSiteRepository.findAll()) {
            for (City city : cityRepository.findAll()) {
                List<Indication> forecasts = indicationRepository.findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(webSite, city, true, dateStart, dateEnd);
                Map<Key, List<Indication>> groupedForecasts = groupForecastsByKey(forecasts);
                for (Map.Entry<Key, List<Indication>> groupForecasts : groupedForecasts.entrySet()) {
                    List<PairForecastObservation> forecastObservationPairs = createForecastObservationPairs(groupForecasts.getValue());
                    if (!forecastObservationPairs.isEmpty()) {
                        groupingPairs.put(groupForecasts.getKey(), forecastObservationPairs);
                    }
                }
            }
        }
        log.debug("результат: {}", groupingPairs);
        return groupingPairs;
    }

    private Map<Key, List<Indication>> groupForecastsByKey(List<Indication> forecasts) {
        log.debug("groupForecastsByKey(List<Indication> forecasts), параметры:{}", forecasts);
        Map<Key, List<Indication>> groupedForecasts = new HashMap<>();
        for (Indication forecast : forecasts) {
            Key key = Key.builder()
                    .webSite(forecast.getWebSite())
                    .city(forecast.getCity())
                    .antiquity((int) ChronoUnit.DAYS.between(forecast.getDateIndicate(), forecast.getDateRequest()))
                    .build();
            if (groupedForecasts.get(key) == null) {
                List<Indication> newForecastsList = new ArrayList<>();
                newForecastsList.add(forecast);
                groupedForecasts.put(key, newForecastsList);
            } else {
                groupedForecasts.get(key).add(forecast);
            }
        }
        log.debug("результат: {}", groupedForecasts);
        return groupedForecasts;
    }

    private List<PairForecastObservation> createForecastObservationPairs(List<Indication> forecasts) {
        log.debug("createForecastObservationPairs(List<Indication> forecasts), параметры:{}", forecasts);
        if (forecasts.isEmpty()) {
            return new ArrayList<>();
        }
        TreeSet<Indication> sortedForecasts = new TreeSet<>(forecasts);
        List<Indication> observations = indicationRepository.findByWebSiteIsNullAndCityAndIsForecastAndDateIndicateBetween(
                forecasts.get(0).getCity(),
                false,
                sortedForecasts.first().getDateIndicate().minus(1, ChronoUnit.HOURS),
                sortedForecasts.last().getDateIndicate().plus(1, ChronoUnit.HOURS));
        TreeSet<Indication> sortedObservations = new TreeSet<>(observations);
        List<PairForecastObservation> pairForecastObservations = new ArrayList<>();
        for (Indication forecast : sortedForecasts) {
            Indication nearestObservation = getNearestObservation(sortedObservations, forecast);
            if (nearestObservation != null) {
                pairForecastObservations.add(new PairForecastObservation(forecast, nearestObservation));
            }
        }
        log.debug("результат: {}", pairForecastObservations);
        return pairForecastObservations;
    }

    private Statistic calculateStatistic(Key key, List<PairForecastObservation> forecastObservationList) {
        log.debug("calculateStatistic(Key key, List<PairForecastObservation> forecastObservationList), параметры:{},{}", key, forecastObservationList);
        List<PairNumber> sequenceOfTemperature = forecastObservationList.stream()
                .map(x -> new PairNumber<Double>(x.getForecast().getTemperature(),
                        x.getObservation().getTemperature()))
                .collect(Collectors.toList());
        List<PairNumber> sequenceOfIntensity = forecastObservationList.stream()
                .map(x -> new PairNumber<Integer>(x.getForecast().getIntensity().ordinal(),
                        x.getObservation().getIntensity().ordinal()))
                .collect(Collectors.toList());
        double standartDeviationOfTemperature = standartDeviationCalculator.calculateStandardDeviation(sequenceOfTemperature);
        double standartDeviationOfIntensity = standartDeviationCalculator.calculateStandardDeviation(sequenceOfIntensity);
        Statistic statistic = Statistic.builder()
                .standartDeviationTemperature(standartDeviationOfTemperature)
                .standartDeviationIntencity(standartDeviationOfIntensity)
                .antiquity(key.antiquity)
                .webSite(key.webSite)
                .city(key.city)
                .build();
        log.debug("результат: {}", statistic);
        return statistic;
    }

    @Builder
    @EqualsAndHashCode
    private static class Key {
        int antiquity;
        WebSite webSite;
        City city;
    }

    private Indication getNearestObservation(TreeSet<Indication> observations, Indication forecast) {
        log.debug("getNearestObservation(TreeSet<Indication> observations, Indication forecast), параметры:{},{}", observations, forecast);
        Indication lowerObservation = observations.lower(forecast);
        Indication higherObservation = observations.higher(forecast);

        if (lowerObservation != null && higherObservation != null) {
            long deltaObservation1 = lowerObservation.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            long deltaObservation2 = higherObservation.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            Indication nearestObservation = deltaObservation1 < deltaObservation2 ? lowerObservation : higherObservation;
            if (ChronoUnit.HOURS.between(nearestObservation.getDateIndicate(), forecast.getDateIndicate()) < 1) {
                log.debug("результат:{}", nearestObservation);
                return nearestObservation;
            }
        } else if (lowerObservation != null && ChronoUnit.HOURS.between(lowerObservation.getDateIndicate(), forecast.getDateIndicate()) < 1) {
            log.debug("результат:{}", lowerObservation);
            return lowerObservation;
        } else if (higherObservation != null && ChronoUnit.HOURS.between(higherObservation.getDateIndicate(), forecast.getDateIndicate()) < 1) {
            log.debug("результат:{}", higherObservation);
            return higherObservation;
        }
        return null;
    }
}

