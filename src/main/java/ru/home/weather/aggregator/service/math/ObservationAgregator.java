package ru.home.weather.aggregator.service.math;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.Intensity;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.repository.IndicationRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Elena Demeneva
 */
@Service
@Log4j2
public class ObservationAgregator {
    @Autowired
    IndicationRepository indicationRepository;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    StandartDeviationCalculator standartDeviationCalculator;

    public List<Indication> getAverageObservations(LocalDate dateStart, LocalDate dateEnd) {
        log.debug("getAverageObservations(LocalDate dateStart, LocalDate dateEnd), параметры:{},{}", dateStart, dateEnd);
        List<Indication> averageObservations = new ArrayList<>();
        for (City city : cityRepository.findAll()) {
            TreeMap<Instant, List<Indication>> observationsGroupedByTime = getObservationsGroupedByTime(city, dateStart, dateEnd);
            for (Map.Entry<Instant, List<Indication>> entry : observationsGroupedByTime.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    averageObservations.add(createAverageObservation(entry.getValue(), entry.getKey(), city));
                }
            }
        }
        log.debug("результат: {}", averageObservations);
        return averageObservations;
    }

    private TreeMap<Instant, List<Indication>> getObservationsGroupedByTime(City city, LocalDate dateStart, LocalDate dateEnd) {
        log.debug("getObservationsGroupedByTime(City city, LocalDate dateStart, LocalDate dateEnd, параметры:{},{},{}", city, dateStart, dateEnd);
        TreeMap<Instant, List<Indication>> observationsGroupedByTime = createEmptyTimesMap(dateStart, dateEnd);
        List<Indication> observations = indicationRepository.findByCityAndIsForecastAndDateIndicateBetween(
                city,
                false,
                dateStart.atStartOfDay().toInstant(ZoneOffset.UTC),
                dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC));
        for (Indication observation : observations) {
            Instant key = getNearestTime(observationsGroupedByTime, observation.getDateIndicate());
            if (key != null) {
                observationsGroupedByTime.get(key).add(observation);
            }
        }
        log.debug("результат: {}", observationsGroupedByTime);
        return observationsGroupedByTime;
    }

    private Indication createAverageObservation(List<Indication> observations, Instant dateIndicate, City city) {
        log.debug("createAverageObservation(List<Indication> observations, Instant dateTime, City city), параметры:{},{},{}", observations, dateIndicate, city);
        double averageTemperature = standartDeviationCalculator.calculateAverage(observations.stream().map(Indication::getTemperature).collect(Collectors.toList()));
        double averageIntensity = standartDeviationCalculator.calculateAverage(observations.stream().map(x -> x.getIntensity().ordinal()).collect(Collectors.toList()));
        Indication averageObservation = Indication.builder()
                .dateIndicate(dateIndicate)
                .temperature(averageTemperature)
                .intensity(Intensity.values()[(int) Math.round(averageIntensity)])
                .isForecast(false)
                .city(city)
                .build();
        log.debug("результат: {}", averageObservation);
        return averageObservation;
    }

    private TreeMap<Instant, List<Indication>> createEmptyTimesMap(LocalDate dateStart, LocalDate dateEnd) {
        log.debug("createEmptyTimesMap(LocalDate dateStart, LocalDate dateEnd), параметры:{},{}", dateStart, dateEnd);
        Instant nextTime = dateStart.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant lastTime = dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC);
        TreeMap<Instant, List<Indication>> timesMap = new TreeMap<>();
        while (nextTime.compareTo(lastTime) < 0) {
            timesMap.put(nextTime, new ArrayList<>());
            nextTime = nextTime.plus(3, ChronoUnit.HOURS);
        }
        log.debug("результат: {}", timesMap);
        return timesMap;
    }

    private Instant getNearestTime(TreeMap<Instant, List<Indication>> timesMap, Instant dateIndicate) {
        log.debug("getNearestTime(TreeMap<Instant, List<Indication>> timesMap, Instant dateIndicate), параметры:{},{}", timesMap, dateIndicate);
        Instant lowerTime = timesMap.lowerKey(dateIndicate);
        Instant higherTime = timesMap.higherKey(dateIndicate);
        if (lowerTime != null && higherTime != null) {
            long deltaLowerTime = lowerTime.getEpochSecond() - dateIndicate.getEpochSecond();
            long deltaHigherTime = higherTime.getEpochSecond() - dateIndicate.getEpochSecond();
            Instant resultTime = deltaLowerTime < deltaHigherTime ? lowerTime : higherTime;
            if (ChronoUnit.HOURS.between(resultTime, dateIndicate) < 3) {
                log.debug("результат: {}", resultTime);
                return resultTime;
            }
        } else if (lowerTime != null && ChronoUnit.HOURS.between(lowerTime, dateIndicate) < 3) {
            log.debug("результат: {}", lowerTime);
            return lowerTime;
        } else if (higherTime != null && ChronoUnit.HOURS.between(higherTime, dateIndicate) < 3) {
            log.debug("результат: {}", higherTime);
            return higherTime;
        }
        return null;
    }
}
