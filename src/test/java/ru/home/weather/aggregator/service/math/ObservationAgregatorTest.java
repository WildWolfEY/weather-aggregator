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
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

@SpringBootTest
class ObservationAgregatorTest {
    @Autowired
    ObservationAgregator observationAgregator;
    @MockBean
    IndicationRepository indicationRepository;
    @MockBean
    CityRepository cityRepository;

    private final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    @Test
    void getAverageObservations() {
        LocalDate dateStart = FMT.parse("2023-06-12 00:00:00", LocalDate::from);
        LocalDate dateEnd = FMT.parse("2023-06-13 00:00:00", LocalDate::from);
        List<City> cities = List.of(new City());
        Mockito.doReturn(cities)
                .when(cityRepository)
                .findAll();

        Mockito.doReturn(prepareIndications())
                .when(indicationRepository).findByCityAndIsForecastAndDateIndicateBetween(
                ArgumentMatchers.any(City.class),
                ArgumentMatchers.anyBoolean(),
                ArgumentMatchers.any(Instant.class),
                ArgumentMatchers.any(Instant.class));

        List<Indication> indications = observationAgregator.getAverageObservations(dateStart, dateEnd);

        Assert.isTrue(indications.get(0).getTemperature() == 6.5, "Неверно усреднили температуру");
        Assert.isTrue(indications.get(1).getTemperature() == 10, "Неверно усреднили температуру");
        Assert.isTrue(indications.get(0).getPrecipitation() == Precipitation.LIGHT, "Неверно усреднили осадки");
        Assert.isTrue(indications.get(1).getPrecipitation() == Precipitation.NORMAL, "Неверно усреднили осадки");
        Assert.isTrue(indications.size() == 4, "Неверно сгруппировали измерения");
    }

    private List<Indication> prepareIndications() {
        Indication i1 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 12:03:00", Instant::from))
                .temperature(10)
                .precipitation(Precipitation.CLEAR)
                .build();

        Indication i2 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 12:13:00", Instant::from))
                .temperature(11)
                .precipitation(Precipitation.HEAVY)
                .build();

        Indication i3 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 13:23:00", Instant::from))
                .temperature(9)
                .precipitation(Precipitation.NORMAL)
                .build();

        Indication i4 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 13:33:00", Instant::from))
                .temperature(19)
                .precipitation(Precipitation.CLEAR)
                .build();

        Indication i5 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 16:43:00", Instant::from))
                .temperature(15)
                .precipitation(Precipitation.LIGHT)
                .build();

        Indication i6 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:03:00", Instant::from))
                .temperature(12)
                .precipitation(Precipitation.CLEAR)
                .build();
        Indication i7 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:08:00", Instant::from))
                .temperature(12)
                .precipitation(Precipitation.CLEAR)
                .build();

        Indication i8 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:30:00", Instant::from))
                .temperature(10)
                .precipitation(Precipitation.LIGHT)
                .build();

        Indication i9 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 17:41:00", Instant::from))
                .temperature(10)
                .precipitation(Precipitation.HEAVY)
                .build();

        Indication i10 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 00:00:00", Instant::from))
                .temperature(7)
                .precipitation(Precipitation.NORMAL)
                .build();

        Indication i11 = Indication.builder()
                .dateIndicate(FMT.parse("2023-06-12 00:01:00", Instant::from))
                .temperature(6)
                .precipitation(Precipitation.CLEAR)
                .build();

        Indication i12 = Indication.builder()
                .dateIndicate(FMT.parse("2023-07-12 00:01:00", Instant::from))
                .temperature(5)
                .precipitation(Precipitation.CLEAR)
                .build();

        Indication i13 = Indication.builder()
                .dateIndicate(FMT.parse("2023-07-12 00:00:00", Instant::from))
                .temperature(5)
                .precipitation(Precipitation.HEAVY)
                .build();
        return List.of(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13);
    }
}