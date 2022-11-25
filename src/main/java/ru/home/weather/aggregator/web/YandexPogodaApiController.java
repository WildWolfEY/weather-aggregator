package ru.home.weather.aggregator.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.service.YandexPogodaParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class YandexPogodaApiController implements ApiController {

    @Autowired
    YandexPogodaParser parser;


    @Override
    public List<Indication> getForecasts(City city) {
        try {
            Document htmlContent = getContent(city);
            List<Indication> indications = parser.parseForecastIndications(htmlContent);
            for (Indication indication : indications) {
                indication.setCity(city);
            }
            return indications;
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка при выполнении запроса к серверу ");
        }
    }

    @Override
    public Indication getObservation(City city) {
        return null;
    }

    private Document getContent(City city) throws IOException {
        return Jsoup.connect(URI.create("https://yandex.ru/pogoda/details/10-day-weather?" +
                "lat=" + city.getLatitude() +
                "&lon=" + city.getLongitude()).toString())
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();
    }
}
