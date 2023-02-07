package ru.home.weather.aggregator.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.service.math.RatingCalculator;

import javax.persistence.NamedNativeQuery;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Elena Demeneva
 */

public interface StatisticRepository extends CrudRepository<Statistic,Long> {
    List<Statistic> findAll();
    List<Statistic> findByCity(City city);
    List<Statistic> findByCityAndPrescription(City city, int prescription);
    List<Statistic> findByPrescription( int prescription);
    List<Statistic> findByStartPeriodAndEndPeriod(LocalDate startPeriod, LocalDate endPeriod);

//    @Query(value = "select avg(t.t) as first, avg(t.i) as second " +
//            "from " +
//            "(select 1/avg(s.standart_deviation_temperature) as t, 1/avg(s.standart_deviation_intencity) as i from statistic s  " +
//            "group by prescription, id_website, id_city) t", nativeQuery = true)


    @Query(nativeQuery = true, name = "findAllDataMapping")
            List<Statistic> findAllOfStatistic();
}
