package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.Intensity;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@Log4j2
public class YandexPogodaParser implements WeatherDataParser<Document, String> {
    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    IntensityDeterminant intensityDeterminant;
    private final URI url = URI.create("https://yandex.ru/pogoda/");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Indication> parseForecastIndications(Document htmlContent) {
        log.debug("parseForecastIndications(Document htmlContent)");
        log.trace("параметр {}", htmlContent.text());
        Elements days = htmlContent.getElementsByTag("article");
        List<Indication> indications = new ArrayList<>();
        int dayNumber = 0;
        for (Element divDay : days) {
            log.debug("content from <article>: {}", divDay.text());
            Calendar date = createDay(dayNumber++);
            Elements weatherTableRow = divDay.getElementsByClass("weather-table__row");
            int numberPartOfDay = 0;
            for (Element dayPart : weatherTableRow) {
                log.debug("content from <class='weather-table__row'>: {}", dayPart.text());
                numberPartOfDay++;
                Element weatherTableTemperature = dayPart.getElementsByClass("weather-table__temp").get(0);
                Elements temperatures = weatherTableTemperature.getElementsByClass("temp__value temp__value_with-unit");
                int partOfNumberPartOfDay = 0;
                for (Element temperature : temperatures) {
                    log.debug("content from <class='temp__value temp__value_with-unit'>: {}", temperature.text());
                    partOfNumberPartOfDay++;
                    addTime(date, numberPartOfDay, partOfNumberPartOfDay);
                    Instant dateForecast = Instant.ofEpochMilli(date.getTimeInMillis());
                    String condition = dayPart.getElementsByClass("weather-table__body-cell weather-table__body-cell_type_condition").first().text();
                    if (dateForecast.isAfter(Instant.now())) {
                        Indication indication = createIndication(
                                dateForecast,
                                Float.valueOf(temperature.text().replace("−", "-")),
                                intensityDeterminant.getIntensity(condition)
                        );

                        indication.setForecast(true);
                        indications.add(indication);
                    }
                }
            }
        }
        log.debug("результат: {}", indications);
        return indications;
    }

    @Override
    public Indication parseObservationIndication(String responseBody) throws JsonProcessingException {
        log.debug("parseObservationIndication(String responseBody) параметр:{}", responseBody);
        WeatherData weatherData = objectMapper.readValue(responseBody, WeatherData.class);
        Instant dataIndication = Instant.ofEpochSecond(weatherData.getFact().obs_time);
        Indication indication = createIndication(dataIndication, weatherData.getFact().temp, intensityDeterminant.getIntensity(weatherData.fact.condition));
        indication.setForecast(false);
        log.debug("результат:{}", indication);
        return indication;
    }

    private Calendar createDay(int countDay) {
        log.debug("Calendar createDay(int countDay), параметр: {}", countDay);
        LocalDate date = LocalDate.now();
        Calendar calendar = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DATE, countDay);
        log.debug("результат: {}", calendar);
        return calendar;
    }

    private void addTime(Calendar date, int partOfDay, int partOfPart) {
        log.debug("addTime(Calendar date, int partOfDay, int partOfPart), параметры: {},{},{}", date, partOfDay, partOfPart);
        if (partOfDay == 1 && partOfPart == 1) {
            date.add(Calendar.HOUR, 6);
        } else {
            date.add(Calendar.HOUR, 3);
        }
        log.debug("результат {}", date);
    }

    private Indication createIndication(Instant dateIndicate, float temperature, Intensity intensity) {
        log.debug("createIndication(Instant dateIndicate, float temperature, Intensity intensity), параметры: {},{},{}", dateIndicate, temperature, intensity);
        return Indication.builder()
                .dateRequest(Instant.now())
                .dateIndicate(dateIndicate)
                .temperature(temperature)
                .intensity(intensity)
                .webSite(webSiteRepository.findByHttp(url.toString())
                        .orElseGet(() -> saveWebSite()))
                .build();
    }

    private WebSite saveWebSite() {
        log.debug("saveWebSite()");
        WebSite webSite = webSiteRepository.save(WebSite.builder()
                .http(url.toString())
                .title("YandexPogoda").build());
        log.info("результат: в БД сохранен новый webSite {}", webSite);
        return webSite;
    }


    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class WeatherData {
        String now_dt;
        ObservationRaw fact;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    private static class ObservationRaw {
        float temp;
        String condition;
        long obs_time;
    }
}
