package ru.home.weather.aggregator.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.service.MaptilerParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;

/**
 * @author Elena Demeneva
 */

@Service
@Log4j2
public class MaptilerApiController {
    // https://api.maptiler.com/geocoding/55.27166,53.981815.json?key=KcLHTxo3tsLwt07PyTK5
    // https://docs.maptiler.com/cloud/api/geocoding/ - там ссылка на генератор URL-ов
    private final HttpClient client = HttpClient.newBuilder().build();
    private final String token = "KcLHTxo3tsLwt07PyTK5";
    @Autowired
    MaptilerParser parser;

    public List<City> getCities(String cityName, String area, String country) {
        log.debug("getCities(String cityName, String area, String country), параметры {},{},{}", cityName, area, country);
        int httpStatus;
        try {
            HttpResponse<String> httpResponse = getCityHttpResponse(cityName, area, country);
            httpStatus = httpResponse.statusCode();
            if (httpStatus == 200) {
                return parser.parseCities(httpResponse.body(), country);
            }
        } catch (JsonProcessingException exception) {
            log.warn("ошибка {} {}", exception.toString(), exception.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка парсинга ответа от api.maptiler.com");
        } catch (Exception exception) {
            log.warn("ошибка {} {}", exception.toString(), exception.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка при выполнении запроса к серверу api.maptiler.com");
        }
        log.warn("ошибка в ответе от сервера api.maptiler.com httpStatus ={}", httpStatus);
        throw new HttpClientErrorException(HttpStatus.valueOf(httpStatus), "Ошибка в ответе от сервера api.maptiler.com");
    }

    private HttpResponse<String> getCityHttpResponse(String cityName, String area, String country)
            throws InterruptedException, IOException {
        log.debug("getCityHttpResponse(String cityName, String area, String country), параметры {},{},{}"
                , cityName, area, country);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.maptiler.com/geocoding/" +
                        cityName + "%20" +
                        area.replaceAll(" ", "%20") + "%20" +
                        (country.isBlank() ? Locale.getDefault().getDisplayCountry() : country) +
                        ".json?key=" + token))
                .GET()
                .build();
        log.debug("обращаемся к api по url = {}", request.uri());
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
