package ru.home.weather.aggregator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

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
