package org.example.smartsensorproject.service;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.example.smartsensorproject.model.Anomaly;
import org.example.smartsensorproject.model.SensorReading;
import org.example.smartsensorproject.repository.AnomalyRepository;
import org.example.smartsensorproject.repository.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AnomalyDetectionService {
    @Autowired
    private SensorReadingRepository sensorReadingRepository; // Припускаємо, що ви маєте такий репозиторій
    @Autowired
    private AnomalyRepository anomalyRepository;
    @Autowired
    private GreenhouseSettingService settingService;
    // Кількість точок для розрахунку (наприклад, останні 100)
    private static final int HISTORY_LIMIT = 100;

    // Основний метод для перевірки нового значення
    public void checkAnomaly(SensorReading latestReading) {
        String sensorTypeName = String.valueOf(latestReading.getSensorType());
        double value = latestReading.getValue();

        // 1. Отримати поріг Z-Score (наприклад, 3.0)
        // Використовуємо String-назву для отримання налаштувань
        double zScoreMultiplier = settingService.getZScoreMultiplier(sensorTypeName);

        // 2. Отримати історичні дані для розрахунку середнього та відхилення
        // Передаємо String-назву типу сенсора у репозиторій
        List<SensorReading> history = sensorReadingRepository.findLatestReadings(sensorTypeName, 100); // Останні 100 значень

        // 2. Використовуємо Apache Commons Math для статистики
        DescriptiveStatistics stats = new DescriptiveStatistics();
        history.stream().map(SensorReading::getValue).forEach(stats::addValue);
        // 3.
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        // Уникнення ділення на нуль, якщо stdDev = 0 (усі значення однакові)
        if (stdDev == 0.0) return;
        // 4. Розрахунок Z-Score
        double zScore = (value - mean) / stdDev;
        // 5. Перевірка на аномалію
        if (Math.abs(zScore) > zScoreMultiplier) {

            // 5. Збереження аномалії
            Anomaly anomaly = new Anomaly();
            anomaly.setTimestamp(latestReading.getTimestamp());
            anomaly.setSensorType(sensorTypeName);
            anomaly.setDetectedValue(value);
            anomaly.setZScoreValue(zScore);
            anomaly.setAnomalyType("Значення відхилилося від норми більше ніж на "
                    + zScoreMultiplier
                    + " стандартних відхилень.");
            anomalyRepository.save(anomaly);

            // Оповіщення (тимчасово в консоль)
            System.out.println("!!! ANOMALY DETECTED !!!: " + sensorTypeName + " Z-Score: " +  String.format("%.2f", zScore));
        }
    }
}
