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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        List<Indication> averageObservations = new ArrayList<>();
        for (City city : cityRepository.findAll()) {
            List<Indication> observations = indicationRepository.findByCityAndIsForecastAndDateIndicateBetween(
                    city,
                    false,
                    dateStart.atStartOfDay().toInstant(ZoneOffset.UTC),
                    dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC));
            TreeMap<Instant, List<Indication>> timesMap = getTimesMap(dateStart, dateEnd);
            for (Indication observation : observations) {
                Instant key = getNearestTime(timesMap, observation.getDateIndicate());
                if (key != null) {
                    timesMap.get(key).add(observation);
                }
            }
            for (Map.Entry<Instant, List<Indication>> entry : timesMap.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    averageObservations.add(createAverageObservation(entry.getValue(), entry.getKey(), city));
                }
            }
        }
        return averageObservations;
    }

    private Indication createAverageObservation(List<Indication> observations, Instant dateTime, City city) {
        List<Double> temperatures = new ArrayList<>();
        List<Integer> intensities = new ArrayList<>();

        for (Indication observation : observations) {
            temperatures.add(observation.getTemperature());
            intensities.add(observation.getIntensity().ordinal());
        }
        double averageTemperature = standartDeviationCalculator.calculateAverage(temperatures);
        double averageIntensity = standartDeviationCalculator.calculateAverage(intensities);

        return Indication.builder()
                .dateIndicate(dateTime)
                .temperature(averageTemperature)
                .intensity(Intensity.values()[(int) Math.round(averageIntensity)])
                .isForecast(false)
                .city(city)
                .build();
    }

    private TreeMap<Instant, List<Indication>> getTimesMap(LocalDate dateStart, LocalDate dateEnd) {
        Instant nextTime = dateStart.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant lastTime = dateEnd.atStartOfDay().toInstant(ZoneOffset.UTC);
        TreeMap<Instant, List<Indication>> timesMap = new TreeMap<>();
        while (nextTime.compareTo(lastTime) < 0) {
            timesMap.put(nextTime, new ArrayList<>());
            nextTime = nextTime.plusSeconds(3 * 60 * 60);
        }
        return timesMap;
    }

    private Instant getNearestTime(TreeMap<Instant, List<Indication>> timesMap, Instant dateIndicate) {
        Instant lowerTime = timesMap.lowerKey(dateIndicate);
        Instant higherTime = timesMap.higherKey(dateIndicate);
        if (lowerTime != null && higherTime != null) {
            long deltaLowerTime = lowerTime.getEpochSecond() - dateIndicate.getEpochSecond();
            long deltaHigherTime = higherTime.getEpochSecond() - dateIndicate.getEpochSecond();
            Instant resultTime = deltaLowerTime < deltaHigherTime ? lowerTime : higherTime;
            if (resultTime.getEpochSecond() - dateIndicate.getEpochSecond() < 3 * 60 * 60) {
                return resultTime;
            }
        } else if (lowerTime != null && (lowerTime.getEpochSecond() - dateIndicate.getEpochSecond() < 3 * 60 * 60)) {
            return lowerTime;
        } else if (higherTime != null && (higherTime.getEpochSecond() - dateIndicate.getEpochSecond() < 3 * 60 * 60)) {
            return higherTime;
        }
        return null;
    }
}
