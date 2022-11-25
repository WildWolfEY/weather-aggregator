package ru.home.weather.aggregator.domain;

import lombok.*;

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
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Indication {
    @Id
    @Column(name = "id_indication")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private float temperature;
    private float millimeters;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_website")
    @Setter
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
