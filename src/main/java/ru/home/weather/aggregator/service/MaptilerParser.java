package ru.home.weather.aggregator.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.City;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Elena Demeneva
 */
@Service
@Log4j2
public class MaptilerParser {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    Map<String, String> countries;

    public List<City> parseCities(String responseBody, String country) throws JsonProcessingException {
        log.debug("parseCities(String responseBody), параметр {}", responseBody);
        List<City> cities = new ArrayList<>();
        Locale currentLocale = Locale.getDefault();
        FeatureCollection featureCollection = objectMapper.readValue(responseBody, FeatureCollection.class);
        log.trace("features: {}", featureCollection);
        for (Feature cityRaw : featureCollection.features) {
            log.trace("cityRaw: {}", cityRaw);
            if (cityRaw.allKeyWordsIsFound(featureCollection.getQuery())) {
                Set<String> localNames = new HashSet<>();
                if (cityRaw.getNameRu() != null) {
                    localNames.add(cityRaw.getNameRu());
                }
                if (cityRaw.getName() != null) {
                    localNames.add(cityRaw.getName());
                }
//            if (cityRaw.getNameRu() != null) {
//                localNames.add(cityRaw.getNameRu());
//            }
//            if (cityRaw.getNameEn() != null) {
//                localNames.add(cityRaw.getNameEn());
//            }
                City city = City.builder()
                        .names(localNames)
                        .area(cityRaw.findArea().isPresent() ? cityRaw.findArea().get().getText() : null)
                        .district(cityRaw.findDistrict().isPresent() ? cityRaw.findDistrict().get().getText() : null)
                        .country(countries.get(country))
                        .longitude(cityRaw.getCenter()[0])
                        .latitude(cityRaw.getCenter()[1])
                        .placeNameRu(cityRaw.matching_place_name == null ? cityRaw.place_name : cityRaw.matching_place_name)
                        .build();
                city.toJson();
                cities.add(city);
                log.trace("adding city: {}", city);
            }
        }
        log.debug("результат: {}", cities);
        return cities;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeatureCollection {
        private Feature[] features;
        private String[] query;

//        protected List<Feature> findCities() {
//            List<Feature> cities = new ArrayList<>();
//            for (Feature feature : features) {
//                if (feature.isCityType()) {
//                    cities.add(feature);
//                }
//            }
//            return cities;
//        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Feature {
        private float[] center;
        private Context[] context;
        @JsonProperty(value = "text")
        String name;
        String place_name;
        // String[] place_type;
        Properties properties;
        @JsonProperty(value = "matching_text")
        String nameRu;
        String matching_place_name;
//        @JsonProperty(value = "text_ru")
//        String nameRu;
//        @JsonProperty(value = "text_en")
//        String nameEn;
//        @JsonProperty(value = "place_name_ru")
//        String descriptionRu;
//        @JsonProperty(value = "place_name_en")
//        String descriptionEn;

        protected Optional<Context> findArea() {
            return Optional.ofNullable(find("level04."));
        }

        protected Optional<Context> findDistrict() {
            return Optional.ofNullable(find("level06."));
        }

        protected Optional<Context> findCountry() {
            return Optional.ofNullable(find("country."));
        }

        protected boolean isCityType() {
            //List<String> types = Arrays.asList(place_type);
            return properties.getType().toLowerCase().equals("town") ||
                    properties.getType().toLowerCase().equals("village") ||
                    properties.getType().toLowerCase().equals("city") ||
                    properties.getType().toLowerCase().equals("admin") ||
                    properties.getType().toLowerCase().equals("place");
        }

        protected boolean allKeyWordsIsFound(String[] keyWords) {
            boolean[] resultArray = new boolean[keyWords.length];
            for (int i = 0; i < resultArray.length; i++) {
                resultArray[i] = (name != null && name.toLowerCase().equals(keyWords[i])) ||
                        (nameRu != null && nameRu.toLowerCase().equals(keyWords[i]));
                if (!resultArray[i]) {

                    resultArray[i] = (place_name != null && place_name.toLowerCase().contains(keyWords[i].toLowerCase())) ||
                            (matching_place_name != null && matching_place_name.toLowerCase().contains(keyWords[i].toLowerCase()));
                }
                if (!resultArray[i] && context != null) {
                    for (Context item : context) {
                        resultArray[i] = item.getText().toLowerCase().contains(keyWords[i].toLowerCase());
                        if (resultArray[i]) {
                            break;
                        }
                    }
                }
                if (!resultArray[i])
                    return false;
            }

            return true;
        }

        private Context find(String level) {
            for (Context item : context) {
                if (item.id.startsWith(level)) {
                    return item;
                }
            }
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Geometry {
        private float[] coordinates;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Context {
        private String id;
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Properties {
        private String type;
    }
}
