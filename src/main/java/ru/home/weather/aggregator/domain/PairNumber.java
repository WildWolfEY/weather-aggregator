package ru.home.weather.aggregator.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Elena Demeneva
 */
@Getter
@Setter
public class PairNumber<T extends Number> {
    private T first;
    private T second;
    @Getter
    private boolean secondExists;

    public void setSecond(T second) throws IllegalArgumentException {
        if(!second.getClass().equals(first.getClass()))
            throw new IllegalArgumentException(second.getClass().toString() + " does not match the class of the first element" );
        this.second = second;
        secondExists = true;
    }

    public PairNumber(T first, T second) throws IllegalArgumentException {
        this.first = first;
        setSecond(second);
    }
}
