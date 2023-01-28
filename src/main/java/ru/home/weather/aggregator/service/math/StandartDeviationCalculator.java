package ru.home.weather.aggregator.service.math;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StandartDeviationCalculator {

    private double calculateStandardDeviation(Map<? extends Number, ? extends Number> sequence) {
        Optional<? extends Map.Entry<? extends Number, ? extends Number>> firstEntry = sequence.entrySet().stream().findFirst();
        double average = 0;
        double sum = 0;
        if (firstEntry.isPresent() && firstEntry.get().getValue() == null) {
            if (firstEntry.get().getKey() instanceof Integer) {
                average = sequence.keySet().stream().mapToDouble(x -> x.intValue()).sum();
            }
            if (firstEntry.get().getKey() instanceof Float) {
                average = sequence.keySet().stream().mapToDouble(x -> x.floatValue()).sum();
            }
            if (firstEntry.get().getKey() instanceof Double) {
                average = sequence.keySet().stream().mapToDouble(x -> x.doubleValue()).sum();
            }
            if (firstEntry.get().getKey() instanceof Long) {
                average = sequence.keySet().stream().mapToDouble(x -> x.longValue()).sum();
            }
        }
        if (firstEntry.get().getValue() == null) {
            for (Map.Entry<? extends Number, ? extends Number> el : sequence.entrySet()) {
                double delta = 0;
                if (el.getKey() instanceof Integer) {
                    delta = el.getKey().intValue() - average;
                } else if (el.getKey() instanceof Float) {
                    delta = el.getKey().floatValue() - average;
                } else if (el.getKey() instanceof Double) {
                    delta = el.getKey().doubleValue() - average;
                } else if (el.getKey() instanceof Long) {
                    delta = el.getKey().longValue() - average;
                }
                sum += Math.pow(delta, 2);
            }
        } else {
            for (Map.Entry<? extends Number, ? extends Number> el : sequence.entrySet()) {
                double delta = 0;
                if (el.getKey() instanceof Integer) {
                    delta = el.getKey().intValue() - el.getValue().intValue();
                } else if (el.getKey() instanceof Float) {
                    delta = el.getKey().floatValue() - el.getValue().floatValue();
                } else if (el.getKey() instanceof Double) {
                    delta = el.getKey().doubleValue() - el.getValue().doubleValue();
                } else if (el.getKey() instanceof Long) {
                    delta = el.getKey().longValue() - el.getValue().longValue();
                }
                sum += Math.pow(delta, 2);
            }
        }
        double result = Math.sqrt(sum / sequence.size());
        return result;
    }
}
