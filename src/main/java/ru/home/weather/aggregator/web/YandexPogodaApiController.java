package ru.home.weather.aggregator.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.service.parser.YandexPogodaParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@Log4j2
public class YandexPogodaApiController implements WeatherApiController {
    @Autowired
    YandexPogodaParser parser;
    private final String token = "2baa4fd6-8f91-4c52-8c96-dc6c4364cf3a";
    protected final HttpClient client = HttpClient.newBuilder().build();

    @Override
    public List<Indication> getForecasts(City city) {
        log.debug("getForecasts(City city), параметр: {}", city);
        try {
            Document htmlContent = getContent(city);
            List<Indication> indications = parser.parseForecastIndications(htmlContent);
            for (Indication indication : indications) {
                indication.setCity(city);
            }
            log.debug("результат: {}", indications);
            return indications;
        } catch (Exception e) {
            log.warn("ошибка {} {}", e.toString(), e.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка при выполнении запроса к серверу ");
        }
    }

    @Override
    public Indication getObservation(City city) {
        log.debug("getObservation(City city), параметр {}", city);
        int httpStatus;
        try {
            HttpResponse<String> httpResponse = getObservationHttpResponse(city);
            httpStatus = httpResponse.statusCode();
            if (httpResponse.statusCode() == 200) {
                Indication indication = parser.parseObservationIndication(httpResponse.body());
                indication.setCity(city);
                return indication;
            }
        } catch (JsonProcessingException exception) {
            log.warn("ошибка {} {}", exception.toString(), exception.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка парсинга ответа от https://api.weather.yandex.ru/v2/informers");
        } catch (Exception exception) {
            log.warn("ошибка {} {}", exception.toString(), exception.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка при выполнении запроса к серверу https://api.weather.yandex.ru/v2/informers");
        }
        log.warn("ошибка в ответе от сервера https://api.weather.yandex.ru/v2/informers httpStatus = {}", httpStatus);
        throw new HttpClientErrorException(HttpStatus.valueOf(httpStatus), "Ошибка в ответе от сервера https://api.weather.yandex.ru/v2/informers");
    }

    private Document getContent(City city) throws IOException {
        log.debug("getContent(City city), параметр {}", city);
        URI uri = URI.create("https://yandex.ru/pogoda/details/10-day-weather?" +
                "lat=" + city.getLatitude() +
                "&lon=" + city.getLongitude());
        log.debug("обращаемся к сайту по url {}", uri);
        return Jsoup.connect(uri.toString())
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();
    }

    private HttpResponse<String> getObservationHttpResponse(City city) throws InterruptedException, IOException {
        log.debug("getObservationHttpResponse(City city), параметр {}", city);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.yandex.ru/v2/informers?" +
                        "lat=" + city.getLatitude() +
                        "&lon=" + city.getLongitude() +
                        "&lang=ru_RU"))
                .header("X-Yandex-API-Key", token)
                .GET()
                .build();
        log.debug("обращаемся к api по url = {}", request.uri());
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
