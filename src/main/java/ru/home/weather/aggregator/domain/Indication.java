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
import java.time.Instant;

@Entity
@Table(name = "indications")
@Builder
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Indication implements Comparable<Indication> {
    @Id
    @Column(name = "id_indication")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private double temperature;
    private Precipitation precipitation;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_website")
    @Setter
    private WebSite webSite;
    private Instant dateRequest;
    @Setter
    private Instant dateIndicate;
    @Setter
    private boolean isForecast;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_city")
    @Setter
    private City city;

    @Override
    public int compareTo(Indication indication) {
        return this.dateIndicate.compareTo(indication.dateIndicate);
    }
}
