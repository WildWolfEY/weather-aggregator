package ru.home.weather.aggregator.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.repository.CityRepository;
import ru.home.weather.aggregator.service.CountryService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Elena Demeneva
 */

@Controller
public class UIController {
    @Autowired
    CountryService countryService;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    OpenWeatherMapApiController openWeatherMapApiController;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("cities", cityRepository.findAll());
        return "main";
    }

    @GetMapping("/city")
    public String goToCity(Model model) {
        model.addAttribute("countries", countryService.getCountries());
        return "add_city";
    }

    @GetMapping("/city/search")
    public String addCity(@RequestParam String name, @RequestParam String country, Model model) {
        try {
            List<City> cities = openWeatherMapApiController.getCities(name, "", country, 10);
            model.addAttribute("cities", cities);
        } catch (Exception exception) {
            System.err.println("Ошибка при поиске города " + exception.getMessage());
            model.addAttribute("exc", exception.getMessage());
        }
        return goToCity(model);
    }

    @GetMapping("/city/add")
    public String addCity(@RequestParam String cityJson, Model model) {
        try {
            City city = objectMapper.readValue(cityJson, City.class);
            cityRepository.save(city);
        } catch (DataIntegrityViolationException exception) {
            System.err.println(exception.getRootCause().getMessage());
            handleException(exception.getRootCause().getMessage(), model);
            return (goToCity(model));
        } catch (Exception exception) {
            handleException(exception.getMessage(), model);
            return (goToCity(model));
        }
        return main(model);
    }

    private void handleException(String message, Model model) {
        System.err.println(message);
        model.addAttribute("exc", message);
    }
}
