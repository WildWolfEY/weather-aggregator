package ru.home.weather.aggregator.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.home.weather.aggregator.domain.City;
import ru.home.weather.aggregator.repository.CityRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Elena Demeneva
 */

@Controller
@Log4j2
public class UIController {
    @Autowired
    Map<String,String > countries;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    MaptilerApiController maptilerApiController;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("cities", cityRepository.findAll());
        return "main";
    }

    @GetMapping("/city")
    public String goToCity(Model model) {
        model.addAttribute("countries", countries);
        return "add_city";
    }

    @GetMapping("/city/search")
    public String searchCity(@RequestParam String name, @RequestParam String area, @RequestParam String country, Model model) {
        try {
            List<City> foundCities = maptilerApiController.getCities(name, area, country);
            model.addAttribute("cities", foundCities);
        } catch (Exception exception) {
            log.warn("Ошибка при поиске города {}, {}", exception.toString(), exception.getMessage());
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
            log.warn("Ошибка при добавлении города {}, {}", exception.toString(), exception.getMessage());
            handleException(exception.getRootCause().getMessage(), model);
            return (goToCity(model));
        } catch (Exception exception) {
            log.warn("Ошибка при добавлении города {}, {}", exception.toString(), exception.getMessage());
            handleException(exception.getMessage(), model);
            return (goToCity(model));
        }
        return main(model);
    }

    private void handleException(String message, Model model) {
        model.addAttribute("exc", message);
    }
}
