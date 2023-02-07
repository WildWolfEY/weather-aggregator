package ru.home.weather.aggregator.web;


import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.service.parser.GisMeteoParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Elena Demeneva
 */

@Service
@Log4j2
public class GisMeteoApiController implements WeatherApiController {
    @Autowired
    GisMeteoParser parser;

    public List<Indication> getForecasts(City city) {
        log.debug("getForecasts(City city), параметр:{}", city);
        List<Indication> indications = new ArrayList<>();
        try {
            getGoogleContent(city);
            List<URI> uriList = createUriFor10Days(parser.findLinkGisMeteo(getGoogleContent(city)));
            for (URI uri : uriList) {
                try {
                    indications.addAll(parser.parseForecastIndications(getGisMeteoContent(uri)));
                } catch (ParseException exception) {
                    log.warn("адрес странички:{}", uri);
                }
            }
        } catch (Exception exception) {
            log.warn("Не удалось корректно выполнить запрос getForecasts(City city) с параметром:{}. Ошибка {},{}", city, exception.toString(), exception.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка при выполнении запроса к серверу ");
        }
        indications.forEach(x -> x.setCity(city));
        log.debug("результат: {}", indications);
        return indications;
    }

    @Override
    public Indication getObservation(City city) {
        log.debug("getObservation(City city), параметр:{}", city);
        Indication indication = new Indication();
        try {
            getGoogleContent(city);
            URI uri = URI.create(parser.findLinkGisMeteo(getGoogleContent(city)).toString().concat("/now"));
            try {
                parser.parseObservationIndication(getGisMeteoContent(uri));
            } catch (ParseException exception) {
                log.warn("адрес странички:{}", uri);
            }
        } catch (Exception exception) {
            log.warn("Не удалось корректно выполнить запрос getForecasts(City city) с параметром:{}. Ошибка {},{}", city, exception.toString(), exception.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка при выполнении запроса к серверу ");
        }
        log.debug("результат: {}", indication);
        return indication;
    }

    private List<URI> createUriFor10Days(URI uri) {
        log.debug("createUriFor10Days(URI uri), параметр:{}", uri);
        List<URI> uriList = new ArrayList<URI>();
        for (int i = 1; i < 11; i++) {
            if (i == 1) {
                uriList.add(URI.create(uri.toString()));
            } else if (i == 2) {
                uriList.add(URI.create(uri.toString()+"/" + "tomorrow/"));
            } else {
                uriList.add(URI.create(uri.toString()+"/" + i + "-day/"));
            }
        }
        log.debug("результат: {}", uriList);
        return uriList;
    }
    //Метод-заглушка
    private Document getGoogleContent2(City city) throws IOException {
        File file = new File("D:\\Лена\\Java\\weather-aggregator\\gismeteo polevskoy.html");
        return Jsoup.parse(file);
    }

    //Метод-заглушка
    private Document getGisMeteoContent2(URI uri) throws IOException {
        File file = new File("D:\\Лена\\Java\\weather-aggregator\\view-source_https___www.gismeteo.ru_weather-colombo-5800_3-day_.html");
        return Jsoup.parse(file);
    }

    private Document getGoogleContent(City city) throws IOException {
        log.debug("getGoogleContent(City city), параметр {}", city);
        URI uri = URI.create("https://www.google.com/search?q=gismeteo+" + city.getNames().stream().findFirst().get());
        log.debug("обращаемся к сайту по url {}", uri);
        Document document = Jsoup.connect(uri.toString())
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .get();
        log.trace("результат:{}", document);
        return document;
    }

    private Document getGisMeteoContent(URI uri) throws IOException {
        log.debug("getGisMeteoContent(URI uri), параметр {}", uri);
        Document document = Jsoup.connect(uri.toString())
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();
        log.trace(document);
        return document;
    }


}
