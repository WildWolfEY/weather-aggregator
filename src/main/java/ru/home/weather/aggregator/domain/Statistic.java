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

import javax.persistence.*;
import java.time.Instant;
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
@EqualsAndHashCode(exclude = {"id", "standartDeviationTemperature","standartDeviationIntencity","prescription"})

@SqlResultSetMapping(
        name = "findAllDataMapping",
        classes = @ConstructorResult(
                targetClass = Statistic.class,
                columns = {
                        @ColumnResult(name = "first"),
                        @ColumnResult(name = "second")
                }
        )
)
@NamedNativeQuery(name = "Statistic.findAllDataMapping", resultClass = Statistic.class, resultSetMapping = "findAllDataMapping",
        query = "select 1/avg(t.t) as first, 1/avg(t.i) as second" +
                "from " +
                "(select avg(s.standart_deviation_temperature) as t, avg(s.standart_deviation_intencity) as i from statistic s  " +
                "group by prescription, id_website, id_city) t"
)
public class Statistic{
    @Id
    @Column(name = "id_statistic")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private double standartDeviationTemperature;
    private double standartDeviationIntencity;
    private int prescription;

    private LocalDate startPeriod;
    private LocalDate endPeriod;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_website")
    private WebSite webSite;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_city")
    private City city;




        double first;
        double second;


}
