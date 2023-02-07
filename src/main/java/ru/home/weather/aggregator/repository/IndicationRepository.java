package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.WebSite;

import java.time.Instant;
import java.util.List;

//https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
public interface IndicationRepository extends CrudRepository<Indication, Long> {
    @Transactional
    void deleteByDateIndicateBefore(Instant expiryDate);

    @Transactional
    void deleteByWebSiteIsNullAndIsForecastAndDateIndicateBetween(boolean isForecast, Instant startDate, Instant endDate);


    List<Indication> findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(WebSite webSite, City city, boolean isForecast, Instant startDate, Instant endDate);

    List<Indication> findByCityAndIsForecastAndDateIndicateBetween(City city, boolean isForecast, Instant startDate, Instant endDate);

    List<Indication> findByWebSiteIsNullAndCityAndIsForecastAndDateIndicateBetween(City city, boolean isForecast, Instant startDate, Instant endDate);
}
