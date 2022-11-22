package ru.home.weather.aggregator.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.home.weather.aggregator.domain.WebSite;
import ru.home.weather.aggregator.repository.IndicationRepository;
import ru.home.weather.aggregator.repository.WebSiteRepository;

@Controller
public class AdminController {

    @Autowired
    WebSiteRepository webSiteRepository;
    @Autowired
    IndicationRepository indicationRepository;

    @GetMapping("/admin")
    public String main(Model model) {
        return getWebSites(model);
    }

    @GetMapping("/admin/websites")
    public String getWebSites(Model model) {
        model.addAttribute("page", "websites");
        model.addAttribute("websites", webSiteRepository.findAll());
        return "admin_panel";
    }

    @GetMapping("/admin/weathers")
    public String getWeathers(Model model) {
        model.addAttribute("page", "weathers");
        model.addAttribute("weathers", indicationRepository.findAll());
        return "admin_panel";
    }

    @GetMapping("/admin/website/add")
    public String addWebSite(@RequestParam String title, @RequestParam String http, Model model) {
        WebSite website = new WebSite();
        website.setTitle(title);
        website.setHttp(http);
        try {
            webSiteRepository.save(website);
        } catch (Exception exception) {
            handleException(exception, model);
        }
        return getWebSites(model);
    }

    private void handleException(Exception exception, Model model) {
        System.err.println(exception.getMessage());
        System.err.println(exception.getCause());
        model.addAttribute("exc", exception.getMessage());
    }
}
