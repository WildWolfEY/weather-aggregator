package ru.home.weather.aggregator.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.home.weather.aggregator.exceptions.crypto.CryptoException;
import ru.home.weather.aggregator.service.Crypto;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.service.parser.OpenWeatherMapParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.List;

@Service
@Log4j2
public class OpenWeatherMapApiController implements WeatherApiController {
    protected final HttpClient client = HttpClient.newBuilder().build();
    private byte[] token = new byte[0];
    @Autowired
    Crypto crypto;
    @Autowired
    private OpenWeatherMapParser parser;
    @Value("${crypto.keystore.openweathermap}")
    String tokenFileName;
    @Autowired
    Charset encoding;

    private byte[] getToken() {
        if (token.length == 0) {
            try {
                token = crypto.getToken(new FileInputStream(tokenFileName));
            } catch (CryptoException | FileNotFoundException e) {
                log.warn(e.getMessage(), e);
            }
        }
        return token;
    }


    @Override
    public List<Indication> getForecasts(City city) {
        log.debug("getForecasts(City city), параметр {}", city);
        int httpStatus;
        try {
            HttpResponse<String> httpResponse = getForecastsHttpResponse(city);
            httpStatus = httpResponse.statusCode();
            if (httpResponse.statusCode() == 200) {
                List<Indication> indications = parser.parseForecastIndications(httpResponse.body());
                for (Indication indication : indications) {
                    indication.setCity(city);
                }
                log.debug("результат {}", indications);
                return indications;
            }
        } catch (JsonProcessingException exception) {
            log.warn("ошибка {} {}", exception.toString(), exception.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка парсинга ответа от api.openweathermap.org");
        } catch (Exception e) {
            log.warn("ошибка {} {}", e.toString(), e.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Ошибка при выполнении запроса к серверу api.openweathermap.org");
        }
        log.warn("ошибка в ответе от сервера api.openweathermap.org httpStatus = {}", httpStatus);
        throw new HttpClientErrorException(HttpStatus.valueOf(httpStatus),
                "Ошибка в ответе от сервера api.openweathermap.org");
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
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка парсинга ответа от api.openweathermap.org");
        } catch (Exception exception) {
            log.warn("ошибка {} {}", exception.toString(), exception.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Ошибка при выполнении запроса к серверу api.openweathermap.org");
        }
        log.warn("ошибка в ответе от сервера api.openweathermap.org httpStatus = {}", httpStatus);
        throw new HttpClientErrorException(HttpStatus.valueOf(httpStatus),
                "Ошибка в ответе от сервера api.openweathermap.org");

    }

    private HttpResponse<String> getForecastsHttpResponse(City city) throws InterruptedException, IOException {
        log.debug("getForecastsHttpResponse(City city), параметр {}", city);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.openweathermap.org/data/2.5/forecast?lat=" +
                        city.getLatitude() +
                        "&lon=" +
                        city.getLongitude() +
                        "&appid=" + new String(getToken(), encoding)))
                .GET()
                .build();
        log.debug("обращаемся к api по url = {}", request.uri());
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getObservationHttpResponse(City city) throws InterruptedException, IOException {
        log.debug("getObservationHttpResponse(City city), параметр {}", city);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.openweathermap.org/data/2.5/weather?lat=" +
                        city.getLatitude() + "&lon=" +
                        city.getLongitude() +
                        "&appid=" +
                        new String(getToken(), encoding)))
                .GET()
                .build();
        log.debug("обращаемся к api по url = {}", request.uri());
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
