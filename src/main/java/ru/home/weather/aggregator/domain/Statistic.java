package ru.home.weather.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

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
@EqualsAndHashCode(exclude = {"id", "standartDeviationTemperature", "standartDeviationIntencity", "antiquity"})
public class Statistic {
    @Id
    @Column(name = "id_statistic")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private double standartDeviationTemperature;
    private double standartDeviationIntencity;
    private int antiquity;
    private LocalDate startPeriod;
    private LocalDate endPeriod;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_website")
    private WebSite webSite;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_city")
    private City city;
}
