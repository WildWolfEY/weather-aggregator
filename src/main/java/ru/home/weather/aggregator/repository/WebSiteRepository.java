package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import ru.home.weather.aggregator.domain.WebSite;

import java.util.Optional;

public interface WebSiteRepository extends CrudRepository<WebSite, Long> {
    Optional<WebSite> findByUrl(String url);
    Optional<WebSite> findByTitle(String title);
}
