package ru.home.weather.aggregator.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.Intensity;

import java.util.Arrays;
import java.util.List;

/**
 * @author Elena Demeneva
 */
@Log4j2
@Service
public class IntensityDeterminant {
    public Intensity getIntensity(String condition) {
        log.debug("getMillimeters(String condition), параметр: {}", condition);
        for(String item: light)
        {
           if(condition.toLowerCase().contains(item)){
               return Intensity.LIGHT;
           }
        }
        for(String item: heavy)
        {
            if(condition.toLowerCase().contains(item)){
                return Intensity.HEAVY;
            }
        }
        for(String item: normal)
        {
            if(condition.toLowerCase().contains(item)){
                return Intensity.NORMAL;
            }
        }
        return Intensity.CLEAR;
    }
    private List<String> light = Arrays.asList("drizzle","морось","light","небольшой");
    private List<String> heavy = Arrays.asList("сильный","ливень","снегопад","heavy","shower");
    private List<String> normal =  Arrays.asList("дождь","снег","град","rain","snow","hail");

}
