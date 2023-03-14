package ru.home.weather.aggregator.service.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.StatisticRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RatingCalculatorTest {
    @Autowired
    RatingCalculator ratingCalculator;
    @MockBean
    StatisticRepository statisticRepository;

    private final WebSite yandex = WebSite.builder().title("Yandex").build();
    private final WebSite openWeatherMap = WebSite.builder().title("OpenWeatherMap").build();
    private final WebSite gisMeteo = WebSite.builder().title("Gismeteo").build();

    @Test
    void getRating() {
        Mockito.doReturn(prepareStatisticData())
                .when(statisticRepository)
                .findByCityAndAntiquity(ArgumentMatchers.any(City.class), ArgumentMatchers.anyInt());
        List<WebSite> webSites = ratingCalculator.getRating(new City(), 1, 0);

        Assert.isTrue(webSites.get(0).equals(openWeatherMap)
                && webSites.get(1).equals(gisMeteo)
                && webSites.get(2).equals(yandex), "Неверный рейтинг");

        webSites = ratingCalculator.getRating(new City(), 1, 1);
        Assert.isTrue(webSites.get(0).equals(gisMeteo)
                && webSites.get(1).equals(openWeatherMap)
                && webSites.get(2).equals(yandex), "Неверный рейтинг");

        webSites = ratingCalculator.getRating(new City(), 1, 2);
        Assert.isTrue(webSites.get(0).equals(openWeatherMap)
                && webSites.get(1).equals(gisMeteo)
                && webSites.get(2).equals(yandex), "Неверный рейтинг");
    }

    private List<Statistic> prepareStatisticData() {
        Statistic s1 = Statistic.builder()
                .antiquity(1)
                .webSite(yandex)
                .standartDeviationTemperature(4.8)
                .standartDeviationIntencity(0.5)
                .build();
        Statistic s2 = Statistic.builder()
                .antiquity(1)
                .webSite(openWeatherMap)
                .standartDeviationTemperature(3.5)
                .standartDeviationIntencity(0.2)
                .build();
        Statistic s3 = Statistic.builder()
                .antiquity(1)
                .webSite(yandex)
                .standartDeviationTemperature(4.2)
                .standartDeviationIntencity(0.3)
                .build();
        Statistic s4 = Statistic.builder()
                .antiquity(1)
                .webSite(gisMeteo)
                .standartDeviationTemperature(4.1)
                .standartDeviationIntencity(0.1)
                .build();
        Statistic s5 = Statistic.builder()
                .antiquity(1)
                .webSite(gisMeteo)
                .standartDeviationTemperature(4.6)
                .standartDeviationIntencity(0.4)
                .build();
        Statistic s6 = Statistic.builder()
                .antiquity(1)
                .webSite(gisMeteo)
                .standartDeviationTemperature(5.8)
                .standartDeviationIntencity(0.2)
                .build();
        Statistic s7 = Statistic.builder()
                .antiquity(1)
                .webSite(yandex)
                .standartDeviationTemperature(4.7)
                .standartDeviationIntencity(0.5)
                .build();
        Statistic s8 = Statistic.builder()
                .antiquity(1)
                .webSite(openWeatherMap)
                .standartDeviationTemperature(4.4)
                .standartDeviationIntencity(0.1)
                .build();
        Statistic s9 = Statistic.builder()
                .antiquity(1)
                .webSite(gisMeteo)
                .standartDeviationTemperature(5.2)
                .standartDeviationIntencity(0.3)
                .build();
        Statistic s10 = Statistic.builder()
                .antiquity(1)
                .webSite(gisMeteo)
                .standartDeviationTemperature(5.1)
                .standartDeviationIntencity(0.5)
                .build();
        Statistic s11 = Statistic.builder()
                .antiquity(1)
                .webSite(yandex)
                .standartDeviationTemperature(8.8)
                .standartDeviationIntencity(0.3)
                .build();
        Statistic s12 = Statistic.builder()
                .antiquity(1)
                .webSite(openWeatherMap)
                .standartDeviationTemperature(2.8)
                .standartDeviationIntencity(0.2)
                .build();
        Statistic s13 = Statistic.builder()
                .antiquity(1)
                .webSite(yandex)
                .standartDeviationTemperature(0.8)
                .standartDeviationIntencity(0.1)
                .build();
        return List.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13);
    }
}