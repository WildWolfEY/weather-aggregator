package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.home.weather.aggregator.domain.City;

@Repository
public interface CityRepository extends CrudRepository<City, Long> {
}
