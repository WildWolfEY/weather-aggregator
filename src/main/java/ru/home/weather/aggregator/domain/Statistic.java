package ru.home.weather.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.util.Pair;

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
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Statistic {
    @Id
    @Column(name = "id_statistic")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private double standartDeviationTemperature;
    private double standartDeviationIntencity;
    private Instant startPeriod;
    private Instant endPeriod;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_website")
    private WebSite webSite;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_city")
    private City city;
    private int countDays;
}
