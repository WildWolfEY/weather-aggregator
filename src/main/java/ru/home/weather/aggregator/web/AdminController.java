package ru.home.weather.aggregator.web;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.home.weather.aggregator.domain.Indication;
import ru.home.weather.aggregator.service.Crypto;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

@Controller
@Log4j2
public class AdminController {
    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    IndicationRepository indicationRepository;

    @GetMapping("/admin")
    public String main(Model model) {
        return goToWebSitePage(model);
    }

    @GetMapping("/admin/websites")
    public String goToWebSitePage(Model model) {
        model.addAttribute("page", "websites");
        model.addAttribute("websites", webSiteRepository.findAll());
        return "admin_panel";
    }

    @GetMapping("/admin/weathers")
    public String goToWeatherPage(Model model,
                                  @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC)
                                  Pageable pageable) {
        model.addAttribute("page", "weathers");
        Page<Indication> page = indicationRepository.findAll(pageable);

        model.addAttribute("webs", webSiteRepository.findAll());
        model.addAttribute("weathers", page);
        model.addAttribute("url", "/admin/weathers");
        return "admin_panel";
    }

    @GetMapping("/admin/weather/filter")
    public String goToFilteredWeatherPage(@RequestParam String website, Model model,
                                          @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC)
                                          Pageable pageable) {
        model.addAttribute("page", "weathers");
        WebSite webSite = webSiteRepository.findByTitle(website).get();
        Page<Indication> page = indicationRepository.findByWebSite(webSite, pageable);
        model.addAttribute("webs", webSiteRepository.findAll());
        model.addAttribute("weathers", page);
        model.addAttribute("url", "/admin/weathers");
        return "admin_panel";
    }

    @GetMapping("/admin/website/add")
    public String addWebSite(@RequestParam String title, @RequestParam String url, Model model) {
        WebSite website = WebSite.builder()
                .title(title.isBlank() ? null : title)
                .url(url.isBlank() ? null : url)
                .build();
        try {
            webSiteRepository.save(website);
        } catch (Exception exception) {
            handleException(exception, model);
        }
        return goToWebSitePage(model);
    }

    private void handleException(Exception exception, Model model) {
        log.warn(exception.getMessage(), exception);
        model.addAttribute("exc", exception.getMessage());
    }

    @Autowired
    Crypto crypto;
    @Value("${crypto.keystore.openweathermap}")
    String openweathermapTokenFileName;
    @Value("${crypto.keystore.yandex}")
    String yandexTokenFileName;
    @Autowired
    Charset charset;

    @GetMapping("/admin/keys")
    public String goToKeyPage(Model model) {
        model.addAttribute("page", "keys");
        return "admin_panel";
    }

    @GetMapping("/admin/encrypt/yandex")
    public String encryptYandexToken(@RequestParam String yandextoken, Model model) {
        try {
            new File(yandexTokenFileName).createNewFile();
            crypto.encryptToken(yandextoken.getBytes(charset), new FileOutputStream(yandexTokenFileName));
        } catch (Exception e) {
            handleException(e, model);
        }
        return goToKeyPage(model);
    }

    @GetMapping("/admin/encrypt/open-weather-map")
    public String encryptOpenWeatherMapToken(@RequestParam String openweathermaptoken, Model model) {
        try {
            new File(openweathermapTokenFileName).createNewFile();
            crypto.encryptToken(openweathermaptoken.getBytes(charset), new FileOutputStream(openweathermapTokenFileName));
        } catch (Exception e) {
            handleException(e, model);
        }
        return goToKeyPage(model);
    }
}
