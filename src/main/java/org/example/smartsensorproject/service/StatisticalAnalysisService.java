package org.example.smartsensorproject.service;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StatisticalAnalysisService {
//середнє
    public double calculateMean(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        return stats.getMean();
    }
    //максимум
    public double findMax(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
    }
    //       мінімум
    public double findMin(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
    }
    //       відхилення
    public double calculateVariance(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        return stats.getVariance();
    }
// дисперсію
    public double calculateStandardDeviation(List<Double> values) {
        return Math.sqrt(calculateVariance(values));
    }

    // Метод для видалення викидів з використанням IQR
    public List<Double> removeOutliersIQR(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        double q1 = stats.getPercentile(25);
        double q3 = stats.getPercentile(75);
        double iqr = q3 - q1;
        double lowerBound = q1 - (1.5 * iqr);
        double upperBound = q3 + (1.5 * iqr);
        return values.stream().filter(v -> v >= lowerBound && v <= upperBound).toList();
    }
    //       кореляцію
    public double calculateCorrelation(List<Double> x, List<Double> y) {
        double[] xArray = x.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yArray = y.stream().mapToDouble(Double::doubleValue).toArray();
        PearsonsCorrelation correlation = new PearsonsCorrelation();
        return correlation.correlation(xArray, yArray);
    }

    // Метод для ковзного середнього
    public List<Double> movingAverage(List<Double> values, int windowSize) {
        return IntStream.range(0, values.size() - windowSize + 1)
                .mapToObj(i -> values.subList(i, i + windowSize))
                .map(subList -> subList.stream().mapToDouble(Double::doubleValue).average().orElse(0))
                .mapToDouble(Double::doubleValue)
                .boxed()
                .toList();
    }
    // медіани
    public double calculateMedian(List<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        return stats.getPercentile(50);
    }
// моди
    public double calculateMode(List<Double> values) {
        Map<Double, Long> frequencyMap = values.stream().collect(Collectors.groupingBy(v -> v, Collectors.counting()));
        return frequencyMap.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(Double.NaN);
    }
// лінійної регресії
    public double[] calculateLinearRegression(List<Double> x, List<Double> y) {
        double[] xArray = x.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yArray = y.stream().mapToDouble(Double::doubleValue).toArray();
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < xArray.length; i++) {
            regression.addData(xArray[i], yArray[i]);
        }
        return new double[]{regression.getSlope(), regression.getIntercept()};
    }

    // Метод для обчислення коефіцієнту детермінації
    public double calculateR2(List<Double> x, List<Double> y) {
        PearsonsCorrelation correlation = new PearsonsCorrelation();
        double[] xArray = x.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yArray = y.stream().mapToDouble(Double::doubleValue).toArray();
        return Math.pow(correlation.correlation(xArray, yArray), 2);
    }

}
