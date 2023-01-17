package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.Pair;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.StatisticRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Elena Demeneva
 */

@Service
@Log4j2
public class Analyser {
    @Autowired
    IndicationRepository indicationRepository;
    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    StatisticRepository statisticRepository;


//    public List<WebSite> getOrderedWebSite(int day) {
//        TreeSet<WebSite> webSites;
//        Map<Double, Double> temperatureMap = new HashMap<>();
//        for(WebSite website : webSiteRepository.findAll()) {
//                for (Statistic statistic : statisticRepository.findByWebSiteAndDateEndBefore(website, )) {
//                    temperatureMap.put(statistic.getStandartDeviationTemperature(), null);
//
//            }
//        }
//        calculateStandardDeviation(temperatureMap);
//    }

    public void collectStatistics() {
        for (WebSite webSite : webSiteRepository.findAll()) {
            for (City city : cityRepository.findAll()) {
                statisticRepository.saveAll(collectStatisticsFor10Days(city, webSite));
            }
        }
    }

    public List<Statistic> collectStatisticsFor10Days(City city, WebSite webSite) {
        log.debug("collectStatisticsFor10Days(City city, WebSite webSite), параметры {},{}", city, webSite);
        LocalDate date = LocalDate.now();
        List<Statistic> statisticList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Instant dateEnd = date.minusDays(i).atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant dateStart = date.minusDays(i + 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
            TreeSet<Indication> forecasts = new TreeSet<>();
            forecasts.addAll(indicationRepository.findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(webSite, city, true, dateStart, dateEnd));
            TreeSet<Indication> observations = getMiddleObservations(city, dateStart, dateEnd);
            TreeSet<Pair> forecastWithObservation = new TreeSet<>(new Comparator<Pair>() {
                @Override
                public int compare(Pair pair1, Pair pair2) {
                    return pair1.getDays() >= pair2.getDays() ? 1 : -1;
                }
            });

            for (Indication forecast : forecasts) {
                Optional<Indication> observation = getNearestObservation(observations, forecast);
                if (observation.isPresent()) {
                    forecastWithObservation.add(new Pair(forecast, observation.get()));
                }
                // log.debug("put: {},{}", pair);
            }
            List<Statistic> statistics = getStatistic(forecastWithObservation);
            if (!statistics.isEmpty()) {
                statisticList.addAll(statistics);
            }
            for(Statistic statistic : statisticList)
            {
                statistic.setStartPeriod(dateStart);
                statistic.setEndPeriod(dateEnd);
                statistic.setCity(city);
                statistic.setWebSite(webSite);
            }
        }
        log.debug("результат: {}", statisticList);
        return statisticList;
    }
//    public List<Statistic> collectStatisticsFor10Days(City city, WebSite webSite) {
//        log.debug("collectStatisticsFor10Days(City city, WebSite webSite), параметры {},{}", city, webSite);
//        LocalDate date = LocalDate.now();
//        List<Statistic> statisticList = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            Instant dateEnd = date.minusDays(i).atStartOfDay(ZoneId.of("UTC")).toInstant();
//            Instant dateStart = date.minusDays(i + 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
//            TreeSet<Indication> forecasts = new TreeSet<>();
//            forecasts.addAll(indicationRepository.findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(webSite, city, true, dateStart, dateEnd));
//            TreeSet<Indication> observations = getMiddleObservations(city, dateStart, dateEnd);
//            TreeMap<Indication, Indication> forecastWithObservation = new TreeMap<>();
//            for (Indication forecast : forecasts) {
//                for(int j = 0; j<10; j++) {
//
//                        Optional<Indication> observation = getNearestObservation(observations, forecast);
//                        if (observation.isPresent())
//                            forecastWithObservation.put(forecast, observation.get());
//                        log.debug("put: {},{}", forecast, observation);
//
//                }
//            }
//            Optional<Statistic> statistic = getStatistic(forecastWithObservation);
//            if (statistic.isPresent()) {
//                statistic.get().setStartPeriod(dateStart);
//                statistic.get().setEndPeriod(dateEnd);
//                statistic.get().setCity(city);
//                statistic.get().setWebSite(webSite);
//                statisticList.add(statistic.get());
//
//            }
//        }
//        log.debug("результат: {}", statisticList);
//        return statisticList;
//    }

    private int getCountForecastDays(Indication forecast) {
        return LocalDateTime.ofEpochSecond(forecast.getDateRequest().getEpochSecond() - forecast.getDateIndicate().getEpochSecond(), 0, null).getHour() / 24;
    }

    // Не доделан.
    private TreeSet<Indication> getMiddleObservations(City city, Instant dateStart, Instant dateEnd) {
        TreeSet<Indication> observations = new TreeSet();
        observations.addAll(indicationRepository.findByCityAndIsForecastAndDateIndicateBetween(city, false, dateStart, dateEnd));
        return observations;
    }

    private Optional<Indication> getNearestObservation(TreeSet<Indication> observations, Indication forecast) {
        Indication observation1 = observations.lower(forecast);
        Indication observation2 = observations.higher(forecast);
        Indication observation = null;
        if (observation1 != null && observation2 != null) {
            long deltaObservation1 = observation1.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            long deltaObservation2 = observation2.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond();
            observation = deltaObservation1 < deltaObservation2 ? observation1 : observation2;
            if (observation1.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond() < 60 * 60) {
                return Optional.of(observation);
            }
        } else if (observation1 != null && (observation1.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond() < 60 * 60)) {
            return Optional.of(observation1);
        } else if (observation2 != null && (observation2.getDateIndicate().getEpochSecond() - forecast.getDateIndicate().getEpochSecond() < 60 * 60)) {
            return Optional.of(observation2);
        }
        return Optional.ofNullable(observation);
    }

    private List<Statistic> getStatistic(TreeSet<Pair> forecastWithObservation) {
        log.debug("getStatistic(Set<Pair> forecastWithObservation))");
        log.trace("параметр: {}", forecastWithObservation);

        List<Statistic> statistics = new ArrayList<>();
        if (forecastWithObservation.isEmpty()) {
            return statistics;
        }
        int days = 0;
        Map<Float, Float> temperatureMap = new HashMap<>();
        Map<Integer, Integer> intensityMap = new HashMap<>();
        for (Pair pair : forecastWithObservation) {
            if (days == pair.getDays()) {
                temperatureMap.put(pair.getForecast().getTemperature(), pair.getObservation().getTemperature());
                intensityMap.put(pair.getForecast().getIntensity().ordinal(), pair.getObservation().getIntensity().ordinal());

            } else {
                Statistic statistic = calculate(temperatureMap, intensityMap);
                statistic.setCountDays(days);
                statistics.add(statistic);
                temperatureMap.clear();
                intensityMap.clear();
                days = pair.getDays();
            }
        }
        if(!temperatureMap.isEmpty())
        {
            Statistic statistic = calculate(temperatureMap, intensityMap);
            statistic.setCountDays(days);
            statistics.add(statistic);
            temperatureMap.clear();
            intensityMap.clear();
        }



        log.debug("результат:{}", statistics);
        return statistics;
    }

    private Statistic calculate(Map<Float, Float> temperatureMap, Map<Integer, Integer> intensityMap) {

        double resultTemperature = calculateStandardDeviation(temperatureMap);
        double resultIntensity = calculateStandardDeviation(intensityMap);
        log.debug("результат:{};{}", resultTemperature, resultIntensity);
        Statistic statistic = Statistic.builder()
                .standartDeviationTemperature(resultTemperature)
                .standartDeviationIntencity(resultIntensity)
                .build();
        return statistic;
    }
//
//    private Optional<Statistic> getStatistic(TreeMap<Indication, Indication> forecastWithObservation) {
//        log.debug("getStatistic(TreeMap<Indication, Indication> forecastWithObservation)");
//        log.trace("параметр: {}", forecastWithObservation);
//        Statistic statistic = null;
//        if (forecastWithObservation.isEmpty()) {
//            return Optional.ofNullable(statistic);
//        }
//        Map<Float, Float> temperatureMap = new HashMap<>();
//        Map<Integer, Integer> intensityMap = new HashMap<>();
//        for (Map.Entry<Indication, Indication> entry : forecastWithObservation.entrySet()) {
//            temperatureMap.put(entry.getKey().getTemperature(), entry.getValue().getTemperature());
//            intensityMap.put(entry.getKey().getIntensity().ordinal(), entry.getValue().getIntensity().ordinal());
//
//        }
//        double resultTemperature = calculateStandardDeviation(temperatureMap);
//        double resultIntensity = calculateStandardDeviation(intensityMap);
//        log.debug("результат:{};{}", resultTemperature, resultIntensity);
//        statistic = Statistic.builder()
//                .standartDeviationTemperature(resultTemperature)
//                .standartDeviationIntencity(resultIntensity)
//                .build();
//        log.debug("результат:{}", statistic);
//        return Optional.of(statistic);
//    }

    private double calculateStandardDeviation(Map<? extends Number, ? extends Number> sequence) {
        Optional<? extends Map.Entry<? extends Number, ? extends Number>> firstEntry = sequence.entrySet().stream().findFirst();
        double average = 0;
        double sum = 0;
        if (firstEntry.isPresent() && firstEntry.get().getValue() == null) {
            if (firstEntry.get().getKey() instanceof Integer) {
                average = sequence.keySet().stream().mapToDouble(x -> x.intValue()).sum();
            }
            if (firstEntry.get().getKey() instanceof Float) {
                average = sequence.keySet().stream().mapToDouble(x -> x.floatValue()).sum();
            }
            if (firstEntry.get().getKey() instanceof Double) {
                average = sequence.keySet().stream().mapToDouble(x -> x.doubleValue()).sum();
            }
            if (firstEntry.get().getKey() instanceof Long) {
                average = sequence.keySet().stream().mapToDouble(x -> x.longValue()).sum();
            }
        }
        if (firstEntry.get().getValue() == null) {
            for (Map.Entry<? extends Number, ? extends Number> el : sequence.entrySet()) {
                double delta = 0;
                if (el.getKey() instanceof Integer) {
                    delta = el.getKey().intValue() - average;
                } else if (el.getKey() instanceof Float) {
                    delta = el.getKey().floatValue() - average;
                } else if (el.getKey() instanceof Double) {
                    delta = el.getKey().doubleValue() - average;
                } else if (el.getKey() instanceof Long) {
                    delta = el.getKey().longValue() - average;
                }
                sum += Math.pow(delta, 2);
            }
        } else {
            for (Map.Entry<? extends Number, ? extends Number> el : sequence.entrySet()) {
                double delta = 0;
                if (el.getKey() instanceof Integer) {
                    delta = el.getKey().intValue() - el.getValue().intValue();
                } else if (el.getKey() instanceof Float) {
                    delta = el.getKey().floatValue() - el.getValue().floatValue();
                } else if (el.getKey() instanceof Double) {
                    delta = el.getKey().doubleValue() - el.getValue().doubleValue();
                } else if (el.getKey() instanceof Long) {
                    delta = el.getKey().longValue() - el.getValue().longValue();
                }
                sum += Math.pow(delta, 2);
            }
        }
        double result = Math.sqrt(sum / sequence.size());
        return result;
    }
}
