package ru.home.weather.aggregator.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class PairNumber<T extends Number> {
    private T firstNumber;
    private T secondNumber;
    private boolean secondExists;

    public PairNumber(T firstNumber, T secondNumber) {
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
    }
}
