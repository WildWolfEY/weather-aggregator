package ru.home.weather.aggregator.service.math;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.home.weather.aggregator.domain.PairNumber;

import java.util.List;

@Service
@Log4j2
public class StandartDeviationCalculator {

//    public double calculateStandardDeviation(List<PairNumber> sequence) {
//        Optional<PairNumber> firstPair = sequence.stream().findFirst();
//        double sum = 0;
//        if (firstPair.isPresent() && !firstPair.get().isSecondExists()) {
//            double average = calculateAverage(sequence.stream().map(PairNumber::getSecond).collect(Collectors.toList()));
//            for (PairNumber element : sequence) {
//                double delta = 0;
//                if (element.getFirst() instanceof Integer) {
//                    delta = element.getFirst().intValue() - average;
//                } else if (element.getFirst() instanceof Float) {
//                    delta = element.getFirst().floatValue() - average;
//                } else if (element.getFirst() instanceof Double) {
//                    delta = element.getFirst().doubleValue() - average;
//                } else if (element.getFirst() instanceof Long) {
//                    delta = element.getFirst().longValue() - average;
//                }
//                sum += Math.pow(delta, 2);
//            }
//        } else {
//            for (PairNumber element : sequence) {
//                double delta = 0;
//                if (element.getFirst() instanceof Integer) {
//                    delta = element.getFirst().intValue() - element.getSecond().intValue();
//                } else if (element.getFirst() instanceof Float) {
//                    delta = element.getFirst().floatValue() - element.getSecond().floatValue();
//                } else if (element.getFirst() instanceof Double) {
//                    delta = element.getFirst().doubleValue() - element.getSecond().doubleValue();
//                } else if (element.getFirst() instanceof Long) {
//                    delta = element.getFirst().longValue() - element.getSecond().longValue();
//                }
//                sum += Math.pow(delta, 2);
//            }
//        }
//        double result = Math.sqrt(sum / sequence.size());
//        return result;
//    }
    public double calculateStandardDeviation(List<PairNumber> sequence) {
        double sum = 0;
            for (PairNumber element : sequence) {
                double delta = 0;
                if (element.getFirst() instanceof Integer) {
                    delta = element.getFirst().intValue() - element.getSecond().intValue();
                } else if (element.getFirst() instanceof Float) {
                    delta = element.getFirst().floatValue() - element.getSecond().floatValue();
                } else if (element.getFirst() instanceof Double) {
                    delta = element.getFirst().doubleValue() - element.getSecond().doubleValue();
                } else if (element.getFirst() instanceof Long) {
                    delta = element.getFirst().longValue() - element.getSecond().longValue();
                }
                sum += Math.pow(delta, 2);
            }
        double result = Math.sqrt(sum / sequence.size());
        return result;
    }
    public double calculateStandardDeviationAverage(List<? extends Number> sequence) {
        double average = calculateAverage(sequence);
        double sum = 0;
            for (Number element : sequence) {
                double delta = 0;
                if (element instanceof Integer) {
                    delta = element.intValue() - average;
                } else if (element instanceof Float) {
                    delta = element.floatValue() - average;
                } else if (element instanceof Double) {
                    delta = element.doubleValue() - average;
                } else if (element instanceof Long) {
                    delta = element.longValue() - average;
                }
                sum += Math.pow(delta, 2);
            }
        return Math.sqrt(sum / sequence.size());
    }

    public double calculateAverage(List<? extends Number> sequence) {
        double summa = 0;
        if (sequence.get(0) instanceof Integer) {
            summa = sequence.stream().mapToDouble(Number::intValue).sum();
        } else if (sequence.get(0) instanceof Float) {
            summa = sequence.stream().mapToDouble(Number::floatValue).sum();
        } else if (sequence.get(0) instanceof Double) {
            summa = sequence.stream().mapToDouble(Number::doubleValue).sum();
        } else if (sequence.get(0) instanceof Long) {
            summa = sequence.stream().mapToDouble(Number::longValue).sum();
        }
        return summa/sequence.size();
    }
}
