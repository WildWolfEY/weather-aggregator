package ru.home.weather.aggregator.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

/**
 * @author Elena Demeneva
 */
@Entity
@Table(name = "indications")
@Builder
@ConstructorBinding
@EqualsAndHashCode
@Getter
public class Indication {
    @Id
    @Column(name = "id_indication")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private float temperature;
    private float millimeters;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_website")
    private WebSite webSite;
    private Instant dateRequest;
    private Instant dateIndicate;
    @Setter
    private boolean isForecast;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_city")
    @Setter
    private City city;
}
