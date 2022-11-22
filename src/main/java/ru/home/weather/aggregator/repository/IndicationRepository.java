package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.home.weather.aggregator.domain.Indication;

import java.time.Instant;


public interface IndicationRepository extends CrudRepository<Indication, Long> {
    @Transactional
    void deleteByDateIndicateBefore(Instant expiryDate);
}
