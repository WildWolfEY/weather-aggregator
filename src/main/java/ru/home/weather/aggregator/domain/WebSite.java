package ru.home.weather.aggregator.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Elena Demeneva
 */
@Entity
@Table(name = "website_directory")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WebSite {
    @Id
    @Column(name = "id_website")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;
    @Column(unique = true, nullable = false)
    private String http;
}
