package ru.home.weather.aggregator.service.parser;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.Intensity;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.WebSiteRepository;
import ru.home.weather.aggregator.service.IntensityDeterminant;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author Elena Demeneva
 */

@Service
@Log4j2
public class GisMeteoParser implements WeatherDataParser<Document, Document> {
    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    IntensityDeterminant intensityDeterminant;

    @Override
    public List<Indication> parseForecastIndications(Document document) throws ParseException {
        log.debug("parseForecastIndications(Document document), параметр:{}", document);
        List<Indication> indications = new ArrayList<>();
        ForecastWeatherData forecastWeatherData = getWeatherData(document);
        WebSite webSite = getWebSite();
        for (int i = 0; i < forecastWeatherData.forecastDates.size(); i++) {
            if (forecastWeatherData.forecastDates.get(i) != null) {
                Indication indication = Indication.builder()
                        .dateIndicate(forecastWeatherData.forecastDates.get(i))
                        .temperature(forecastWeatherData.temperature.get(i))
                        .millimeters(forecastWeatherData.millimeters.get(i))
                        .intensity(forecastWeatherData.intensity.get(i))
                        .dateRequest(Instant.now())
                        .isForecast(true)
                        .webSite(webSite)
                        .build();
                indications.add(indication);
                log.debug("adding: {}", indication);
            }
        }
        return indications;
    }

    @Override
    public Indication parseObservationIndication(Document document) throws ParseException {
        log.debug("parseObservationIndication(Document document), параметр:{}", document);
        Element nowWeather = document.getElementsByClass("weathertabs day-0")
                .first()
                .getElementsByClass("weathertab weathertab-block tooltip")
                .first();
        Element temperatureNowElement = nowWeather.getElementsByClass("unit unit_temperature_c").first();
        Indication indication = Indication.builder()
                .dateRequest(Instant.now())
                .dateIndicate(Instant.now())
                .webSite(getWebSite())
                .temperature(toFloat(temperatureNowElement.text()))
                .intensity(intensityDeterminant.getIntensity(nowWeather.attr("data-text")))
                .build();
        log.debug("Результат: {}", indication);
        return indication;
    }

    private ForecastWeatherData getWeatherData(Document document) throws ParseException {
        Element content = document.getElementsByClass("content wrap").first();
        ForecastWeatherData forecastWeatherData = new ForecastWeatherData();
        Element rowTime = content.getElementsByClass("widget-row widget-row-time").first();
        Element temperatureAir = content.getElementsByAttributeValue("data-row", "temperature-air").first();
        Element precipitationBars = content.getElementsByAttributeValue("data-row", "precipitation-bars").first();
        Element precipitationIcons = content.getElementsByAttributeValue("data-row", "icon-tooltip").first();
        if (rowTime != null && temperatureAir != null && temperatureAir != null && precipitationBars != null) {
            fillTimes(rowTime, forecastWeatherData);
            fillTemperatures(temperatureAir, forecastWeatherData);
            fillMillimeters(precipitationBars, forecastWeatherData);
            fillIntensity(precipitationIcons, forecastWeatherData);
            return forecastWeatherData;
        } else {
            log.warn("Не удалось распарсить страничку. times:{}, temperatures:{}, precipitations{}", rowTime, temperatureAir, precipitationBars);
            throw new ParseException("Не удалось распарсить страничку", 0);
        }
    }

    private void fillTimes(Element timesForDay, ForecastWeatherData forecastWeatherData) throws ParseException {
        log.debug("fillTimes(Element timesForDay, WeatherData weatherData), параметры:{},{}", timesForDay, forecastWeatherData);
        List<String> dateTimeRawList = timesForDay.getElementsByClass("row-item").eachAttr("title");
        for (String dateTimeRaw : dateTimeRawList) {
            String[] dateTimeString = dateTimeRaw.split("UTC: ");
            if (dateTimeString.length > 1) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dateTime = dateFormat.parse(dateTimeString[1]);
                Instant instant = Instant.ofEpochMilli(dateTime.getTime());
                forecastWeatherData.forecastDates.add(instant);
            } else {
                forecastWeatherData.forecastDates.add(null);
            }
        }
        log.debug("результат: {}", forecastWeatherData);
    }

    private void fillTemperatures(Element temperaturesForDay, ForecastWeatherData forecastWeatherData) {
        log.debug("fillTemperatures(Element temperaturesForDay, WeatherData weatherData), параметры:{},{}", temperaturesForDay, forecastWeatherData);
        List<String> temperatureRawList = temperaturesForDay.getElementsByClass("unit unit_temperature_c").eachText();
        for (String temperatureString : temperatureRawList) {
            Float temperature = toFloat(temperatureString);
            if (temperature != null) {
                forecastWeatherData.temperature.add(temperature);
            }
        }
        log.debug("результат: {}", forecastWeatherData);
    }

    private Float toFloat(String temperatureString) {
        log.debug("toFloat(String temperatureString), параметр {}", temperatureString);
        if (temperatureString == null) {
            return null;
        }
        try {
            String cleanSymbols = temperatureString.replaceAll("−", "-").replaceAll(",", ".");
            log.debug("результат:{}", cleanSymbols);
            return Float.parseFloat(cleanSymbols);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void fillMillimeters(Element millimetersForDay, ForecastWeatherData forecastWeatherData) {
        log.debug("fillMillimeters(Element millimetersForDay, WeatherData weatherData), параметры:{},{}", millimetersForDay, forecastWeatherData);
        List<String> millimetersRawList = millimetersForDay.getElementsByClass("item-unit").eachText();
        for (String millimeterString : millimetersRawList) {
            Float millimeter = toFloat(millimeterString);
            if (millimeter != null) {
                forecastWeatherData.millimeters.add(millimeter);
            }
        }
        log.debug("результат: {}", forecastWeatherData);
    }

    private void fillIntensity(Element conditionsForDay, ForecastWeatherData forecastWeatherData) {
        log.debug("fillIntensity(Element conditionsForDay, WeatherData weatherData), параметры:{},{}", conditionsForDay, forecastWeatherData);
        List<String> conditionList = conditionsForDay.getElementsByClass("weather-icon tooltip").eachAttr("data-text");
        for (String condition : conditionList) {
            forecastWeatherData.intensity.add(intensityDeterminant.getIntensity(condition));
        }
        log.debug("результат: {}", forecastWeatherData);
    }

    public URI findLinkGisMeteo(Document content) throws IOException {
        log.debug("findLinkGisMeteo(), параметр:{}", content);
        String pattern = "https://www.gismeteo.ru/weather";
        List<String> links = content.select("a").eachAttr("href");
        log.debug("все ссылки: {}", links);
        Optional<String> link = links.stream()
                .filter(x -> x.contains(pattern))
                .findFirst();
        if (link.isEmpty()) {
            log.warn("не нашли нужную ссылку, содержащую {}", pattern);
            throw new NoSuchElementException("не нашли нужную ссылку, содержащую " + pattern);
        }
        String uri = link.get().substring(link.get().indexOf(pattern), link.get().indexOf("&"));
        log.debug("результат: {}", uri);
        return URI.create(uri);
    }

    @Data
    private static class ForecastWeatherData {
        List<Instant> forecastDates = new ArrayList<>();
        List<Float> temperature = new ArrayList<>();
        List<Float> millimeters = new ArrayList<>();
        List<Intensity> intensity = new ArrayList<>();
    }

    @Data
    private static class ObservationWeatherData {
        Instant ObservationDate;
        float temperature;
        Intensity intensity;
    }

    private WebSite getWebSite() {
        return webSiteRepository.findByHttp("https://www.gismeteo.ru/")
                .orElseGet(() -> saveWebSite());
    }

    private WebSite saveWebSite() {
        log.debug("saveWebSite()");
        WebSite webSite = webSiteRepository.save(WebSite.builder()
                .http("https://www.gismeteo.ru/")
                .title("GisMeteo").build());
        log.info("результат: в БД сохранен новый webSite {}", webSite);
        return webSite;
    }
}
