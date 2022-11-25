package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class YandexPogodaParser implements DataParser<Document, String> {
    @Autowired
    WebSiteRepository webSiteRepository;
    private final URI url = URI.create("https://yandex.ru/pogoda/");

    @Override
    public List<Indication> parseForecastIndications(Document htmlContent){
        Elements days = htmlContent.getElementsByTag("article");
        List<Indication> indications = new ArrayList<>();
        int dayNumber = 0;
        for (Element divDay : days) {
            Calendar date = createDay(dayNumber++);
            Elements weatherTableRow = divDay.getElementsByClass("weather-table__row");
            int numberPartOfDay = 0;
            for (Element dayPart : weatherTableRow) {
                numberPartOfDay++;
                Element weatherTableTemperature = dayPart.getElementsByClass("weather-table__temp").get(0);
                Elements temperatures = weatherTableTemperature.getElementsByClass("temp__value temp__value_with-unit");
                int partOfNumberPartOfDay = 0;
                for (Element temperature : temperatures) {
                    partOfNumberPartOfDay++;
                    addTime(date, numberPartOfDay, partOfNumberPartOfDay);
                    Indication indication = Indication.builder()
                            .dateRequest(Instant.now())
                            .dateIndicate(Instant.ofEpochMilli(date.getTimeInMillis()))
                            .temperature(Float.valueOf(temperature.text().replace("−","-")))
                            .millimeters(getMillimeters(dayPart))
                            .isForecast(true)
                            .webSite(getWebSite())
                            .build();
                    indications.add(indication);
                }
            }
        }
        return indications;
    }

    private float getMillimeters(Element div) {
        String declarationOfPrecipitation = div.getElementsByClass("weather-table__body-cell weather-table__body-cell_type_condition").get(0).text();
        if (declarationOfPrecipitation.toLowerCase().contains("морось")) {
            return 0.1f;
        }
        if (declarationOfPrecipitation.toLowerCase().contains("небольшой")) {
            return 0.2f;
        }
        if (declarationOfPrecipitation.toLowerCase().contains("сильный")
                || declarationOfPrecipitation.toLowerCase().contains("ливень")
                || declarationOfPrecipitation.toLowerCase().contains("снегопад")) {
            return 0.8f;
        }
        if (declarationOfPrecipitation.toLowerCase().contains("дождь")
                || declarationOfPrecipitation.toLowerCase().contains("снег")
                || declarationOfPrecipitation.toLowerCase().contains("град")) {
            return 0.4f;
        }
        return 0;
    }


    @Override
    public Indication parseObservationIndication(String data) throws JsonProcessingException, ParseException {
        return null;
    }

    private Calendar createDay(int countDay) {
        //Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        LocalDate date = LocalDate.now();
        Calendar calendar = new GregorianCalendar(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DATE, countDay);
        return calendar;
    }

    private void addTime(Calendar date, int partOfDay, int partOfPart) {
        if (partOfDay == 1 && partOfPart == 1) {
            date.add(Calendar.HOUR, 6);
        } else {
            date.add(Calendar.HOUR, 3);
        }
    }


//    private Indication createIndication() throws JsonProcessingException {
//
//        return Indication.builder()
//                .dateRequest(Instant.now())
//                .dateIndicate(Instant.ofEpochSecond(0))
//                .temperature(0)
//                .millimeters(0)
//                .webSite(getWebSite())
//                .build();
//    }

    private WebSite getWebSite() {
        return webSiteRepository.findByHttp(url.toString())
                .orElseGet(() -> webSiteRepository.save(WebSite.builder()
                        .http(url.toString())
                        .title("YandexPogoda").build()));
    }
}
