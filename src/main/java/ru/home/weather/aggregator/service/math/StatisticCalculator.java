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
import java.util.*;

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
        List<Statistic> statistics = new ArrayList<>();
        List<Statistic> existStatistics = statisticRepository.findByStartPeriodAndEndPeriod(dateStart,dateEnd);
        Map<Key, List<PairForecastObservation>> groupingPairs = getGroupingForecastsAndObservationsForPeriod(
                dateStart.atStartOfDay().toInstant(ZoneOffset.UTC),
                dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC));
        for (Map.Entry<Key, List<PairForecastObservation>> groupForecastObservation : groupingPairs.entrySet()) {
            Statistic statistic = calculateStatistic(groupForecastObservation.getKey(), groupForecastObservation.getValue());
            statistic.setStartPeriod(dateStart);
            statistic.setEndPeriod(dateEnd);
            if(existStatistics.contains(statistic))
            {
                statistic.setId(existStatistics.get(existStatistics.indexOf(statistic)).getId());
            }
            statistics.add(statistic);
        }
        return statistics;
    }


//    public List<Statistic> calculateStatistic(LocalDate dateStart, LocalDate dateEnd) throws IllegalArgumentException {
//        List<Statistic> statistics = new ArrayList();
//        Map<Key, List<PairForecastObservation>> groupingPairs = getGroupingForecastsAndObservationsForPeriod(
//                dateStart.atStartOfDay().toInstant(ZoneOffset.UTC),
//                dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC));
//        for (Map.Entry<Key, List<PairForecastObservation>> groupForecastObservation : groupingPairs.entrySet()) {
//            Statistic statistic = calculateStatistic(groupForecastObservation.getKey(), groupForecastObservation.getValue());
//            statistic.setStartPeriod(dateStart);
//            statistic.setEndPeriod(dateEnd);
//            statistics.add(statistic);
//        }
//        return statistics;
//    }

    private Map<Key, List<PairForecastObservation>> getGroupingForecastsAndObservationsForPeriod(Instant dateStart, Instant dateEnd) {
        Map<Key, List<PairForecastObservation>> groupingPairs = new HashMap<>();
        for (WebSite webSite : webSiteRepository.findAll()) {
            for (City city : cityRepository.findAll()) {
                List<Indication> forecasts = indicationRepository.findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(webSite, city, true, dateStart, dateEnd);
                Map<Key, List<Indication>> groupingForecasts = groupForecasts(forecasts);
                if (!groupingForecasts.isEmpty()) {
                    for( Map.Entry<Key, List<Indication>> groupForecasts:groupingForecasts.entrySet()) {
                        // Map.Entry<Key, List<Indication>> groupForecasts = groupingForecasts.entrySet().iterator().next();
                        List<PairForecastObservation> pairForecastObservations = createPairs(groupForecasts.getValue());
                        if (!pairForecastObservations.isEmpty()) {
                            groupingPairs.put(groupForecasts.getKey(), pairForecastObservations);
                        }
                    }
                }
            }
        }
        return groupingPairs;
    }

    private Map<Key, List<Indication>> groupForecasts(List<Indication> forecasts) {
        Map<Key, List<Indication>> groupingForecasts = new HashMap<>();
        for (Indication forecast : forecasts) {
            Key key = Key.builder()
                    .webSite(forecast.getWebSite())
                    .city(forecast.getCity())
                    .prescription(calculatePrescriptionDays(forecast))
                    .build();
            if (groupingForecasts.get(key) == null) {
                List<Indication> newList = new ArrayList<>();
                newList.add(forecast);
                groupingForecasts.put(key, newList);
            } else {
                groupingForecasts.get(key).add(forecast);
            }
        }
        return groupingForecasts;
    }

    private List<PairForecastObservation> createPairs(List<Indication> forecasts) {
        if (forecasts.isEmpty()) {
            return new ArrayList<>();
        }
        TreeSet<Indication> sortedForecasts = new TreeSet<>();
        sortedForecasts.addAll(forecasts);
        List<Indication> observations = indicationRepository.findByWebSiteIsNullAndCityAndIsForecastAndDateIndicateBetween(
                forecasts.get(0).getCity(),
                false,
                sortedForecasts.first().getDateIndicate().minusSeconds(60 * 60),
                sortedForecasts.last().getDateIndicate().plusSeconds(60 * 60));
        TreeSet<Indication> sortedObservations = new TreeSet<>();
        sortedObservations.addAll(observations);
        List<PairForecastObservation> pairForecastObservations = new ArrayList<>();
        for (Indication forecast : forecasts) {
            Indication nearestObservation = getNearestObservation(sortedObservations, forecast);
            if (nearestObservation != null) {
                PairForecastObservation pairForecastObservation = new PairForecastObservation(forecast, nearestObservation);
                pairForecastObservations.add(pairForecastObservation);
            }
        }
        return pairForecastObservations;
    }

    private Statistic calculateStatistic(Key key, List<PairForecastObservation> forecastObservationList) {
        List<PairNumber> sequenceOfTemperature = new ArrayList<>();
        List<PairNumber> sequenceOfIntensity = new ArrayList<>();
        for (PairForecastObservation pairForecastObservation : forecastObservationList) {
            PairNumber temperaturePairNumber = new PairNumber(pairForecastObservation.getForecast().getTemperature(), pairForecastObservation.getObservation().getTemperature());
            sequenceOfTemperature.add(temperaturePairNumber);
            PairNumber intensityPairNumber = new PairNumber(pairForecastObservation.getForecast().getIntensity().ordinal(), pairForecastObservation.getObservation().getIntensity().ordinal());
            sequenceOfIntensity.add(intensityPairNumber);
        }
        double standartDeviationOfTemperature = standartDeviationCalculator.calculateStandardDeviation(sequenceOfTemperature);
        double standartDeviationOfIntensity = standartDeviationCalculator.calculateStandardDeviation(sequenceOfIntensity);
        Statistic statistic = Statistic.builder().standartDeviationTemperature(standartDeviationOfTemperature)
                .standartDeviationIntencity(standartDeviationOfIntensity)
                .prescription(key.prescription)
                .webSite(key.webSite)
                .city(key.city)
                .build();
        return statistic;
    }


    @Builder
    @EqualsAndHashCode
    private static class Key {
        int prescription;
        WebSite webSite;
        City city;
    }

    private Indication getNearestObservation(TreeSet<Indication> observations, Indication forecast) {
        Indication lowerObservation = observations.lower(forecast);
        Indication higherObservation = observations.higher(forecast);
        Indication observation = null;
        if (lowerObservation != null && higherObservation != null) {
            long deltaObservation1 = lowerObservation.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            long deltaObservation2 = higherObservation.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            observation = deltaObservation1 < deltaObservation2 ? lowerObservation : higherObservation;
            if (deltaLessThan(lowerObservation.getDateIndicate(), forecast.getDateIndicate(), 1)) {
                return observation;
            }
        } else if (lowerObservation != null && deltaLessThan(lowerObservation.getDateIndicate(), forecast.getDateIndicate(), 1)) {
            return lowerObservation;
        } else if (higherObservation != null && deltaLessThan(higherObservation.getDateIndicate(), forecast.getDateIndicate(), 1)) {
            return higherObservation;
        }
        return null;
    }

    private int calculatePrescriptionDays(Indication indication) {
        return Math.abs(Math.round((indication.getDateIndicate().getEpochSecond() - indication.getDateRequest().getEpochSecond()) / (60 * 60 * 24)));
    }

    private boolean deltaLessThan(Instant date1, Instant date2, int hour) {
        return date1.getEpochSecond() - date2.getEpochSecond() < hour * 60 * 60;
    }
}

