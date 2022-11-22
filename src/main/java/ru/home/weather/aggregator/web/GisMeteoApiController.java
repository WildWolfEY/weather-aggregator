package ru.home.weather.aggregator.web;

import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.domain.Indication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * @author Elena Demeneva
 */

@Service
public class GisMeteoApiController implements ApiController {
    @Override
    public List<Indication> getForecasts(City city) {

        //"https://api.gismeteo.net/v2/weather/forecast/aggregate/4368/?days=3"
        return null;
    }

    @Override
    public Indication getObservation(City city) {
        return null;
    }

    public int getCityId(String city) {
        int cityId = -1;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.gismeteo.net/v2/search/cities/?query=" + city))
                .header("X-Gismeteo-Token", "56b30cb255.3443075")
                .timeout(Duration.ofMillis(5000))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Код ответа " + response.statusCode());
            System.out.println("ID " + cityId);
            cityId = Integer.parseInt(response.body());
        } catch (Exception exception) {
            System.err.println(exception);
        }
        return cityId;
    }
}
