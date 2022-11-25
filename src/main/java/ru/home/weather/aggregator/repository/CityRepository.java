package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.home.weather.aggregator.domain.City;

import java.util.List;

/**
 * @author Elena Demeneva
 */
@Repository
public interface CityRepository extends CrudRepository<City, Long> {
}
