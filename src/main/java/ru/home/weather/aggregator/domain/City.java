package ru.home.weather.aggregator.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Elena Demeneva
 */

@Entity
@Table(name = "cities", uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude"}))
@Data
public class City {
    static private ObjectMapper mapper = new ObjectMapper();
    @Id
    @Column(name = "id_city")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ElementCollection
    @CollectionTable(name = "city_names", joinColumns = @JoinColumn(name = "id_city_name"))
    @Column
    private Set<String> names = new HashSet<>();
    private float latitude;
    private float longitude;
    private String country;
    private String area;
    @Transient
    private String json;


    public void setArea(String area) {
        this.area = area.isBlank() ? null : area;
    }

    public void setCountry(String alpha2Code) {
        country = alpha2Code.isBlank() ? Locale.getDefault().getCountry() : alpha2Code;
    }

    public void toJson() {
        try {
            json= mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
            json= "";
        }
    }
}
