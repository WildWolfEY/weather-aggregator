package ru.home.weather.aggregator.repository;

import org.springframework.data.repository.CrudRepository;
import ru.home.weather.aggregator.domain.WebSite;

import java.util.List;

public interface WebSiteRepository extends CrudRepository<WebSite, Long> {
    List<WebSite> findByHttp(String http);
}
