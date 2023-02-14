package ru.home.weather.aggregator.service.math;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.StatisticRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RatingCalculator {
    @Autowired
    StatisticRepository statisticRepository;
    @Autowired
    StandartDeviationCalculator standartDeviationCalculator;

    public List<WebSite> getRating(City city, int antiquity, int moreImportantIndicator) {
        log.debug("getRating(City city, int antiquity, int moreImportantIndicator)), параметры:{},{},{}", city, antiquity, moreImportantIndicator);
        List<Statistic> statistics = getStatisticByCityAndAntiquity(city, antiquity);
        return calculateRating(statistics, moreImportantIndicator);
    }

    private List<Statistic> getStatisticByCityAndAntiquity(City city, int antiquity) {
        log.debug("getStatisticByCityAndAntiquity(City city, int antiquity), параметры:{},{}", city, antiquity);
        if (city != null && antiquity != 0) {
            log.debug("результат: findByCityAndAntiquity");
            return statisticRepository.findByCityAndAntiquity(city, antiquity);
        } else if (city != null) {
            log.debug("результат: findByCity");
            return statisticRepository.findByCity(city);
        } else if (antiquity != 0) {
            log.debug("результат: findByAntiquity");
            return statisticRepository.findByAntiquity(antiquity);
        } else {
            log.debug("результат: findAll");
            return statisticRepository.findAll();
        }
    }

    private List<WebSite> calculateRating(List<Statistic> statistics, int moreImportantIndicator) {
        log.debug("calculateRating(List<Statistic> statistics, int moreImportantIndicator), параметры:{},{}", statistics, moreImportantIndicator);
        Map<WebSite, List<Statistic>> groupingStatistic = groupStatisticByWebsite(statistics);
        Map<WebSite, Double> webSiteAndstandartDeviations = new HashMap<>();
        for (Map.Entry<WebSite, List<Statistic>> groupStatisticByWebSite : groupingStatistic.entrySet()) {
            double value = calculateStandartDeviation(groupStatisticByWebSite.getValue(), moreImportantIndicator);
            webSiteAndstandartDeviations.put(groupStatisticByWebSite.getKey(), value);
            log.debug("adding:{} {}",groupStatisticByWebSite.getKey(), value);
        }
        return webSiteAndstandartDeviations.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private Double calculateStandartDeviation(List<Statistic> groupedStatistic, int moreImportantIndicator) {
        log.debug("calculateStandartDeviation(List<Statistic> groupedStatistic, int moreImportantIndicator), параметры:{},{}", groupedStatistic, moreImportantIndicator);
        double result = 0;
        if (moreImportantIndicator == 1) {
            result = standartDeviationCalculator.calculateAverage(
                    groupedStatistic.stream().map(Statistic::getStandartDeviationTemperature).collect(Collectors.toList()));
        } else if (moreImportantIndicator == 2) {
            result = standartDeviationCalculator.calculateAverage(
                    groupedStatistic.stream().map(Statistic::getStandartDeviationIntencity).collect(Collectors.toList()));

        } else {
            int NORMALIZATION_COEFFICIENT = 10;
            result = (standartDeviationCalculator.calculateAverage(
                    groupedStatistic.stream().map(Statistic::getStandartDeviationTemperature).collect(Collectors.toList())))
                    + (standartDeviationCalculator.calculateAverage(
                    groupedStatistic.stream().map(Statistic::getStandartDeviationIntencity).collect(Collectors.toList())))
                    * NORMALIZATION_COEFFICIENT;

        }
        log.debug("результат: {}", result);
        return result;
    }

    private Map<WebSite, List<Statistic>> groupStatisticByWebsite(List<Statistic> statistics) {
        log.debug("groupStatisticByWebsite(List<Statistic> statistics) , параметры:{}", statistics);
        Map<WebSite, List<Statistic>> groupingStatistic = new HashMap<>();
        for (Statistic statistic : statistics) {
            if (groupingStatistic.containsKey(statistic.getWebSite())) {
                groupingStatistic.get(statistic.getWebSite()).add(statistic);
            } else {
                List<Statistic> currentList = new ArrayList<>();
                currentList.add(statistic);
                groupingStatistic.put(statistic.getWebSite(), currentList);
            }
        }
        log.debug("результат:{}", groupingStatistic);
        return groupingStatistic;
    }
}
