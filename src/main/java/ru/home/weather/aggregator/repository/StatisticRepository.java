package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Statistic;

import java.time.LocalDate;
import java.util.List;

public interface StatisticRepository extends CrudRepository<Statistic, Long> {
    List<Statistic> findAll();

    List<Statistic> findByCity(City city);

    List<Statistic> findByCityAndAntiquity(City city, int antiquity);

    List<Statistic> findByAntiquity(int antiquity);

    List<Statistic> findByStartPeriodAndEndPeriod(LocalDate startPeriod, LocalDate endPeriod);

}
