package ru.home.weather.aggregator.service.math;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import ru.home.weather.aggregator.domain.PairNumber;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class StandartDeviationCalculatorTest {
    @Autowired
    StandartDeviationCalculator calculator;

    @Test
    void calculateStandardDeviation() {
        PairNumber p1 = new PairNumber<>(5,5);
        PairNumber p2 = new PairNumber<>(3,3);
        PairNumber p3 = new PairNumber<>(5,5);
        PairNumber p4 = new PairNumber<>(8,8);
        PairNumber p5 = new PairNumber<>(8,8);

        List<PairNumber> sequence = new ArrayList<>(List.of(p1,p2,p3,p4,p5));
        Assert.isTrue (0d==calculator.calculateStandardDeviation(sequence));
    }

    @Test
    void calculateStandardDeviationAverage() {
        List sequence = new ArrayList(List.of(2,4,4,4,5,5,7,9));
        Assert.isTrue(2==calculator.calculateStandardDeviationAverage(sequence));
    }

    @Test
    void calculateAverage() {
        List sequence = new ArrayList(List.of(2,4,4,4,5,5,7,9));
        Assert.isTrue(5d==calculator.calculateAverage(sequence));
    }
}