package ru.home.weather.aggregator.service.math;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.Precipitation;
import ru.home.weather.aggregator.domain.Statistic;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.StatisticRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class StatisticCalculatorTest {
    @Autowired
    StatisticCalculator statisticCalculator;
    @MockBean
    private StatisticRepository statisticRepository;
    @MockBean
    WebSiteRepository webSiteRepository;
    @MockBean
    CityRepository cityRepository;
    @MockBean
    IndicationRepository indicationRepository;

    private final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter()
            .withZone(ZoneOffset.UTC);
    private final WebSite webSite = WebSite.builder().title("Yandex").build();
    private final City city = new City();


    @Test
    void calculateStatistic() {
        LocalDate dateStart = FMT.parse("2023-06-12 00:00:00", LocalDate::from);
        LocalDate dateEnd = FMT.parse("2023-06-13 00:00:00", LocalDate::from);

        mockObjects();

        List<Statistic> statistics = statisticCalculator.calculateStatistic(dateStart, dateEnd);

        Assert.isTrue(statistics.size()==2, "Неверно сгруппированы измерения по давности");
        Statistic statistic = statistics.stream().filter(x->x.getAntiquity()==6).findFirst().get();
        Assert.isTrue(statistic.getStandartDeviationTemperature() == 4.753945729601885, "Неверно посчитано стандартное отклонение температуры температура");
        Assert.isTrue(statistic.getStandartDeviationIntencity() == 2, "Неверно посчитано стандартное отклонение осадков");
    }

    private void mockObjects() {
        Mockito.doReturn(List.of(city))
                .when(cityRepository)
                .findAll();

        Mockito.doReturn(List.of(webSite))
                .when(webSiteRepository)
                .findAll();

        Mockito.doReturn(new ArrayList<Indication>())
                .when(statisticRepository)
                .findByStartPeriodAndEndPeriod(
                        ArgumentMatchers.any(LocalDate.class),
                        ArgumentMatchers.any(LocalDate.class));
        prepareForecastsData();
        prepareObservationsData();
    }

    private void prepareForecastsData() {
        Indication i1 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 12:03:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(10)
                .precipitation(Precipitation.CLEAR)
                .isForecast(true)
                .build();

        Indication i2 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 12:13:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(11)
                .precipitation(Precipitation.HEAVY)
                .isForecast(true)
                .build();

        Indication i3 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 13:23:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(9)
                .precipitation(Precipitation.NORMAL)
                .isForecast(true)
                .build();

        Indication i4 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 13:33:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(19)
                .precipitation(Precipitation.CLEAR)
                .isForecast(true)
                .build();

        Indication i5 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 16:43:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(15)
                .precipitation(Precipitation.LIGHT)
                .isForecast(true)
                .build();

        Indication i6 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:03:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(12)
                .precipitation(Precipitation.CLEAR)
                .isForecast(true)
                .build();
        Indication i7 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:08:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(12)
                .precipitation(Precipitation.CLEAR)
                .isForecast(true)
                .build();

        Indication i8 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:30:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(10)
                .precipitation(Precipitation.LIGHT)
                .isForecast(true)
                .build();

        Indication i9 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:41:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(10)
                .precipitation(Precipitation.HEAVY)
                .isForecast(true)
                .build();

        Indication i10 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 00:00:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(7)
                .precipitation(Precipitation.NORMAL)
                .isForecast(true)
                .build();

        Indication i11 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 00:01:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(6)
                .precipitation(Precipitation.CLEAR)
                .isForecast(true)
                .build();

        Indication i12 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 15:01:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(5)
                .precipitation(Precipitation.CLEAR)
                .isForecast(true)
                .build();

        Indication i13 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 15:40:00", Instant::from))
                .dateRequest(FMT.parse("2023-06-06 10:00:00", Instant::from))
                .webSite(webSite)
                .city(city)
                .temperature(5)
                .precipitation(Precipitation.HEAVY)
                .isForecast(true)
                .build();

        Mockito.doReturn(List.of(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13))
                .when(indicationRepository)
                .findByWebSiteAndCityAndIsForecastAndDateIndicateBetween(
                        ArgumentMatchers.any(WebSite.class),
                        ArgumentMatchers.any(City.class),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.any(Instant.class),
                        ArgumentMatchers.any(Instant.class));
    }

    private void prepareObservationsData() {
        Indication i7 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 03:00:00", Instant::from))
                .city(city)
                .temperature(5)
                .precipitation(Precipitation.CLEAR)
                .isForecast(false)
                .build();
        Indication i8 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 06:00:00", Instant::from))
                .city(city)
                .temperature(8)
                .precipitation(Precipitation.CLEAR)
                .isForecast(false)
                .build();
        Indication i1 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 09:00:00", Instant::from))
                .city(city)
                .temperature(10)
                .precipitation(Precipitation.CLEAR)
                .isForecast(false)
                .build();

        Indication i2 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 12:00:00", Instant::from))
                .city(city)
                .temperature(12)
                .precipitation(Precipitation.LIGHT)
                .isForecast(false)
                .build();

        Indication i3 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 15:00:00", Instant::from))
                .city(city)
                .temperature(9)
                .precipitation(Precipitation.NORMAL)
                .isForecast(false)
                .build();

        Indication i4 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 18:00:00", Instant::from))
                .city(city)
                .temperature(8)
                .precipitation(Precipitation.HEAVY)
                .isForecast(false)
                .build();

        Indication i5 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 21:00:00", Instant::from))
                .city(city)
                .temperature(6)
                .precipitation(Precipitation.LIGHT)
                .isForecast(false)
                .build();

        Indication i6 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 23:00:00", Instant::from))
                .city(city)
                .temperature(4)
                .precipitation(Precipitation.CLEAR)
                .isForecast(false)
                .build();


        Mockito.doReturn(List.of(i1, i2, i3, i4, i5, i6, i7, i8))
                .when(indicationRepository)
                .findByWebSiteIsNullAndCityAndIsForecastAndDateIndicateBetween(
                        ArgumentMatchers.any(City.class),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.any(Instant.class),
                        ArgumentMatchers.any(Instant.class));
    }
}