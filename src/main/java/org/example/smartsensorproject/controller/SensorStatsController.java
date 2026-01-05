package org.example.smartsensorproject.controller;
import lombok.Getter;
import org.example.smartsensorproject.model.SensorReading;
import org.example.smartsensorproject.model.SensorType;
import org.example.smartsensorproject.service.SensorDataService;
import org.example.smartsensorproject.service.StatisticalAnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SensorStatsController {
    private final SensorDataService sensorDataService;
    private final StatisticalAnalysisService statisticalAnalysisService;

    // Змінимо конструктор, щоб Spring міг автоматично впровадити StatisticalAnalysisService
    public SensorStatsController(SensorDataService sensorDataService, StatisticalAnalysisService statisticalAnalysisService) {
        this.sensorDataService = sensorDataService;
        this.statisticalAnalysisService = statisticalAnalysisService; // Тепер це Spring-Bean
    }

    @GetMapping("/sensors_status")
    public String sensorsStatus(Model model) {
        List<SensorType> sensorTypes = Arrays.asList(SensorType.values());
        model.addAttribute("sensorTypes", sensorTypes);
        return "sensors_status";
    }

    @GetMapping("/sensors_status/{sensorType}")
    public String sensorDetails(@PathVariable SensorType sensorType, Model model) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(1);
        List<SensorReading> sensorData = sensorDataService.getSensorData(sensorType, start, end);

        List<Double> values = sensorData.stream().map(SensorReading::getValue).toList();

        if (values.isEmpty()) {
            // Додайте повідомлення в модель, що немає даних для цього сенсора
            model.addAttribute("message", "No data available for this sensor in the last 24 hours.");
            // Можете повернути іншу view, або просто не відображати графіки
            return "sensor_details"; // Або "no_data_page"
        }

        model.addAttribute("sensorType", sensorType);
// Статистика
        model.addAttribute("mean", statisticalAnalysisService.calculateMean(values));
        model.addAttribute("max", statisticalAnalysisService.findMax(values));
        model.addAttribute("min", statisticalAnalysisService.findMin(values));
        model.addAttribute("stdDev", statisticalAnalysisService.calculateStandardDeviation(values));
        model.addAttribute("median", statisticalAnalysisService.calculateMedian(values));
        model.addAttribute("mode", statisticalAnalysisService.calculateMode(values));

        // Для кореляції та регресії
        if (sensorType != SensorType.AIR_TEMPERATURE) {
            List<SensorReading> tempData = sensorDataService.getSensorData(SensorType.AIR_TEMPERATURE, start, end);
            List<Double> tempValues = tempData.stream().map(SensorReading::getValue).toList();

            // Перевірка на співпадіння розмірів, щоб уникнути помилок
            int commonSize = Math.min(values.size(), tempValues.size());
            List<Double> commonValues = values.subList(0, commonSize);
            List<Double> commonTempValues = tempValues.subList(0, commonSize);

            if (commonSize >= 2) { // Для кореляції та регресії потрібно мінімум 2 точки
                double correlation = statisticalAnalysisService.calculateCorrelation(commonValues, commonTempValues); // X = Temp, Y = Current Sensor
                double[] regression = statisticalAnalysisService.calculateLinearRegression(commonTempValues, commonValues);
                double r2 = statisticalAnalysisService.calculateR2(commonTempValues, commonValues);

                model.addAttribute("correlation", statisticalAnalysisService.calculateCorrelation(commonValues, commonTempValues));
                model.addAttribute("slope", regression[0]);
                model.addAttribute("intercept", regression[1]);
                model.addAttribute("r2", r2); // R2 для X=Temp, Y=Current Sensor
                // **ДОДАЙТЕ ЦІ НОВІ АТРИБУТИ:**
                model.addAttribute("hasCorrelationData", !Double.isNaN(correlation));
                model.addAttribute("hasRegressionData", !Double.isNaN(regression[0]) && !Double.isNaN(regression[1]) && !Double.isNaN(r2));


                // Підготовка даних для Scatter Plot
                List<ChartPoint> scatterData = new ArrayList<>();
                for (int i = 0; i < commonSize; i++) {
                    scatterData.add(new ChartPoint(commonTempValues.get(i), commonValues.get(i)));
                }
                model.addAttribute("scatterData", scatterData);
            } else {
                // Встановіть значення за замовчуванням (NaN) та прапорці на false
                model.addAttribute("correlation", Double.NaN);
                model.addAttribute("slope", Double.NaN);
                model.addAttribute("intercept", Double.NaN);
                model.addAttribute("r2", Double.NaN);
                model.addAttribute("hasCorrelationData", false); // <--- Встановлюємо в false
                model.addAttribute("hasRegressionData", false);   // <--- Встановлюємо в false
                model.addAttribute("scatterData", new ArrayList<>());
            }
        } else {
            // Якщо це AIR_TEMPERATURE, ці атрибути не потрібні або встановлюються в false
            model.addAttribute("hasCorrelationData", false);
            model.addAttribute("hasRegressionData", false);
        }

        // --- Підготовка даних для сенсорного графіка (Moving Average) ---
        // Створюємо список, що містить тільки timestamp (у мілісекундах) і value
        List<SensorChartPoint> chartRawData = sensorData.stream()
                .map(sr -> new SensorChartPoint(sr.getTimestamp(), sr.getValue()))
                .collect(Collectors.toList());
        model.addAttribute("chartRawData", chartRawData); // Передаємо це у модель

        List<Double> movingAverageData = statisticalAnalysisService.movingAverage(values, 5);
        // Вирівнюємо movingAverageData до довжини rawValues (values) для коректного відображення
        // Заповнюємо початок null, щоб Chart.js проігнорував ці точки
        List<Double> paddedMovingAverageData = new ArrayList<>();
        for (int i = 0; i < values.size() - movingAverageData.size(); i++) {
            paddedMovingAverageData.add(null);
        }
        paddedMovingAverageData.addAll(movingAverageData);
        model.addAttribute("movingAverage", paddedMovingAverageData); // Передаємо вирівняні дані
        return "sensor_details";
    }

    @Getter
    public static  class ChartPoint {
        public double x;
        public double y;

        public ChartPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

    }
    @Getter
    public static class SensorChartPoint {
        public long timestampMs; // Мілісекунди з епохи
        public double value;

        public SensorChartPoint(LocalDateTime timestamp, double value) {
            // Перетворюємо LocalDateTime на мілісекунди з урахуванням системного часового поясу
            this.timestampMs = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.value = value;
        }

    }
}
