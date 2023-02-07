package ru.home.weather.aggregator.service.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.StatisticRepository;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
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

    public List<WebSite> getRating(City city, int prescription, int temperatureOrPrecipitation) {
        List<Statistic> statistics = new ArrayList<>();
        if (city != null) {
            if (prescription != 0) {
                statistics = statisticRepository.findByCityAndPrescription(city, prescription);
            } else {
                statistics = statisticRepository.findByCity(city);
            }
        } else if (prescription != 0) {
            statistics = statisticRepository.findByPrescription(prescription);
        } else {
            statistics = statisticRepository.findAll();
        }
        List<WebSite> webSites = calculateRating(statistics, temperatureOrPrecipitation);
        return webSites;
    }

    private List<WebSite> calculateRating(List<Statistic> statistics, int temperatureOrPrecipitation) {
        Map<WebSite, List<Statistic>> groupingStatistic = new HashMap<>();
        Map<WebSite, Double> webSiteAndstandartDeviations = new HashMap<>();
        List<WebSite> webSites = new ArrayList<>();
        for (Statistic statistic : statistics) {
            if (groupingStatistic.containsKey(statistic.getWebSite())) {
                groupingStatistic.get(statistic.getWebSite()).add(statistic);
            } else {
                List<Statistic> currentList = new ArrayList<>();
                currentList.add(statistic);
                groupingStatistic.put(statistic.getWebSite(), currentList);
            }
        }
        for (Map.Entry<WebSite, List<Statistic>> groupStatistic : groupingStatistic.entrySet()) {
            double resultStandartDeviation = 0;
            if (temperatureOrPrecipitation == 1) {
                resultStandartDeviation = standartDeviationCalculator.calculateAverage(
                        groupStatistic.getValue().stream().map(x -> x.getStandartDeviationTemperature()).collect(Collectors.toList()));
            } else if (temperatureOrPrecipitation == 2) {
                resultStandartDeviation =  standartDeviationCalculator.calculateAverage(
                        groupStatistic.getValue().stream().map(x -> x.getStandartDeviationIntencity()).collect(Collectors.toList()));

            } else {

                List<Statistic> coeff = statisticRepository.findAllOfStatistic();
                double k1 = coeff.get(0).getFirst();
                double k2 = coeff.get(0).getSecond();
//               PairNumber coefficients = statisticRepository.calculateCoefficient();
                resultStandartDeviation = ((standartDeviationCalculator.calculateAverage(
                        groupStatistic.getValue().stream().map(x -> x.getStandartDeviationTemperature()).collect(Collectors.toList())))
                        *k1
               + (standartDeviationCalculator.calculateAverage(
                        groupStatistic.getValue().stream().map(x -> x.getStandartDeviationIntencity()).collect(Collectors.toList())))
                *k2)/2;

            }
            webSiteAndstandartDeviations.put(groupStatistic.getKey(), resultStandartDeviation);
        }
        webSites = webSiteAndstandartDeviations.entrySet().stream()
                .sorted(new Comparator<Map.Entry<WebSite, Double>>() {
                    @Override
                    public int compare(Map.Entry<WebSite, Double> o1, Map.Entry<WebSite, Double> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }).map(x -> x.getKey()).collect(Collectors.toList());
        return webSites;
    }

//    @SqlResultSetMapping(name = "customDataMapping",
//            classes = @ConstructorResult(
//                    targetClass = CustomData.class,
//                    columns = {
//                            @ColumnResult(name = "first", type = Double.class),
//                            @ColumnResult(name = "second", type = Double.class),
//                    }
//            )
//    )
//    @NamedNativeQuery(name = "customDataMapping", resultClass = RatingCalculator.CustomData.class, resultSetMapping ="customDataMapping", query =
//            "select avg(t.t) as first, avg(t.i) as second " +
//                    "from " +
//                    "(select 1/avg(s.standart_deviation_temperature) as t, 1/avg(s.standart_deviation_intencity) as i from statistic s  " +
//                    "group by prescription, id_website, id_city) t")



}
