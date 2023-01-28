package ru.home.weather.aggregator.service.math;

import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import ru.home.weather.aggregator.domain.*;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.time.Instant;
import java.util.*;

public class StatisticCalculator {

    @Autowired
    IndicationRepository indicationRepository;
    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    CityRepository cityRepository;

    public List<Statistic> getStatistic(Instant dateStart, Instant dateEnd) {
        Map<Key, List<Pair>>  groupingPairs = getGroupingForecastsAndObservationsForPeriod(dateStart, dateEnd);

        return null;
    }

    private Map<Key, List<Pair>> getGroupingForecastsAndObservationsForPeriod(Instant dateStart, Instant dateEnd) {
        Map<Key, List<Pair>> groupingPairs = new HashMap<>();
        for (WebSite webSite : webSiteRepository.findAll()) {
            for (City city : cityRepository.findAll()) {
                List<Indication> forecasts = indicationRepository.findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(webSite, city, true, dateStart, dateEnd);
                Map<Key, List<Indication>> groupingForecasts = groupForecasts(forecasts);
                if (!groupingForecasts.isEmpty()) {
                    Map.Entry<Key, List<Indication>> entry = groupingForecasts.entrySet().iterator().next();
                    List<Pair> pairs = createPairs(entry.getValue());
                    if (!pairs.isEmpty()) {
                        groupingPairs.put(entry.getKey(), pairs);
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
                    .prescription(calculatePrescription(forecast))
                    .build();
            List<Indication> currentForecastsList = groupingForecasts.get(key);
            if (currentForecastsList != null) {
                currentForecastsList = List.of(forecast);
                groupingForecasts.put(key, currentForecastsList);
            } else {
                currentForecastsList.add(forecast);
            }
        }
        return groupingForecasts;
    }

    private List<Pair> createPairs(List<Indication> forecasts) {
        if (forecasts.isEmpty()) {
            return new ArrayList<>();
        }
        TreeSet<Indication> sortedForecasts = new TreeSet<>();
        sortedForecasts.addAll(forecasts);
        List<Indication> observations = indicationRepository.findByCityAndIsForecastAndDateIndicateBetween(forecasts.get(0).getCity(),
                false,
                sortedForecasts.first().getDateIndicate().minusSeconds(60 * 60),
                sortedForecasts.last().getDateIndicate().plusSeconds(60 * 60));
        TreeSet<Indication> sortedObservations = new TreeSet<>();
        sortedObservations.addAll(observations);
        List<Pair> pairs = new ArrayList<>();
        for (Indication forecast : forecasts) {
            Indication nearestObservation = getNearestObservation(sortedObservations, forecast);
            if (nearestObservation != null) {
                Pair pair = new Pair(forecast, nearestObservation);
                pairs.add(pair);
            }
        }
        return pairs;
    }

    private Statistic calculateStatistic(List<Pair> pairs) {
        return null;
    }

    private int calculatePrescription(Indication indication) {
        return Math.abs(Math.round((indication.getDateIndicate().getEpochSecond() - indication.getDateRequest().getEpochSecond()) / (60 * 60 * 24)));
    }

    @Builder
    private static class Key {
        int prescription;
        WebSite webSite;
        City city;
    }

    private Indication getNearestObservation(TreeSet<Indication> observations, Indication forecast) {
        Indication observation1 = observations.lower(forecast);
        Indication observation2 = observations.higher(forecast);
        Indication observation = null;
        if (observation1 != null && observation2 != null) {
            long deltaObservation1 = observation1.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            long deltaObservation2 = observation2.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            observation = deltaObservation1 < deltaObservation2 ? observation1 : observation2;
            if (observation1.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond() < 60 * 60) {
                return observation;
            }
        } else if (observation1 != null && (observation1.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond() < 60 * 60)) {
            return observation1;
        } else if (observation2 != null && (observation2.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond() < 60 * 60)) {
            return observation2;
        }
        return null;
    }
}

