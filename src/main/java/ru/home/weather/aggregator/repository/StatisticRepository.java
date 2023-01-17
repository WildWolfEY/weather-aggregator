package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.domain.WebSite;

import java.time.Instant;

/**
 * @author Elena Demeneva
 */

public interface StatisticRepository extends CrudRepository<Statistic,Long> {
    Iterable<Statistic> findByWebSiteAndEndPeriodBefore (WebSite webSite, Instant date);

}
