package ru.home.weather.aggregator.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Data
public class WebSite {
    @Id
    @Column(name = "id_website")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter
    private long id;
    private String title;
    @Column(unique = true, nullable = false)
    private String http;

    public void setHttp(String http) {
        this.http = http.trim().isEmpty() ? null : http;
    }

    public void setTitle(String title) {
        this.title = title.isBlank() ? null : title;
    }
}
