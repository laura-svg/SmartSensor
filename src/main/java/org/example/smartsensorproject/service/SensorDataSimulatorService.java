package org.example.smartsensorproject.service;

import org.example.smartsensorproject.model.SensorReading;
import org.example.smartsensorproject.model.SensorType;
import org.example.smartsensorproject.repository.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

@Service
public class SensorDataSimulatorService {

    @Autowired
    private SensorReadingRepository sensorReadingRepository;
    @Autowired
    private AnomalyDetectionService anomalyDetectionService;

    private final SensorDataService sensorDataService;
    private final Random random = new Random();

    private final Map<SensorType, Double> currentSensorValues = new EnumMap<>(SensorType.class);

    // Константи для відхилень та початкових значень. Це дозволить легко налаштовувати симуляцію
    private static final double TEMP_DEVIATION = 0.5; // Зменшимо відхилення для більш плавних змін
    private static final double HUMIDITY_DEVIATION = 1.5;
    private static final double SOIL_MOISTURE_DEVIATION = 2.0;
    private static final double LIGHT_DEVIATION = 2000.0;
    private static final double PH_DEVIATION = 0.05;
    private static final double CO2_DEVIATION = 10.0;

    public SensorDataSimulatorService(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
        initializeSensorValues();
    }

    private void initializeSensorValues() {
        currentSensorValues.put(SensorType.AIR_TEMPERATURE, 22.0);
        currentSensorValues.put(SensorType.LIGHT_INTENSITY, 40000.0); // Ініціалізуємо світло першим, бо воно впливає на температуру
        currentSensorValues.put(SensorType.AIR_HUMIDITY, 60.0);
        currentSensorValues.put(SensorType.SOIL_MOISTURE, 50.0);
        currentSensorValues.put(SensorType.SOIL_PH, 6.5);
        currentSensorValues.put(SensorType.CO2_LEVEL, 500.0);
    }

    @Scheduled(fixedRate = 5000) // Генеруємо дані кожні 5 секунд
    public void simulateSensorData() {
        // Порядок генерації важливий для взаємозалежностей:
        // 1. Світло (впливає на температуру, CO2)
        // 2. Температура (впливає на вологість повітря, CO2, вологість ґрунту)
        // 3. Вологість повітря (може впливати на вологість ґрунту, але робимо її залежною від температури)
        // 4. CO2 (залежить від світла та температури)
        // 5. Вологість ґрунту (залежить від температури повітря, вологості повітря)
        // 6. pH ґрунту (слабо залежить від вологості ґрунту, в основному стабільний)

        generateAndSaveSensorReading(SensorType.LIGHT_INTENSITY, this::generateLightIntensity);
        generateAndSaveSensorReading(SensorType.AIR_TEMPERATURE, this::generateTemperature);
        generateAndSaveSensorReading(SensorType.AIR_HUMIDITY, this::generateHumidity);
        generateAndSaveSensorReading(SensorType.CO2_LEVEL, this::generateCo2Level);
        generateAndSaveSensorReading(SensorType.SOIL_MOISTURE, this::generateSoilMoisture);
        generateAndSaveSensorReading(SensorType.SOIL_PH, this::generateSoilPh);

        // System.out.println("--- End of cycle ---"); // Для відладки
    }

    // Допоміжний метод для генерації та збереження одного показання датчика
    private void generateAndSaveSensorReading(SensorType type, java.util.function.Supplier<Double> generatorFunction) {
        SensorReading reading = new SensorReading();
        reading.setSensorType(type);
        reading.setTimestamp(LocalDateTime.now());
        reading.setGreenhouseId("main");
        double newValue = generatorFunction.get();
        // З 5% шансом вводимо екстремальне, нереалістичне значення
        if (random.nextDouble() < 0.05) {
            double maxRange = type.equals(SensorType.LIGHT_INTENSITY) ? 70000.0 : 100.0; // Макс. можливе значення
            double anomalyMagnitude = maxRange * (0.1 + random.nextDouble() * 0.2); // Викид на 10-30% від макс. діапазону

            // Викид може бути як позитивним, так і негативним
            if (random.nextBoolean()) {
                newValue += anomalyMagnitude;
            } else {
                newValue -= anomalyMagnitude;
            }
            // Обмежуємо значення, щоб вони не були абсолютно нескінченними (але залишалися викидами)
            newValue = Math.max(0, newValue);

            System.out.println("!!! ВВЕДЕННЯ АНОМАЛІЇ: " + type + " -> " + newValue);
        }
        reading.setValue(newValue);
        currentSensorValues.put(type, newValue); // Оновлюємо "поточне" значення після генерації
        sensorDataService.saveSensorData(reading);
        // Перевірка на аномалію ОДРАЗУ після збереження
        anomalyDetectionService.checkAnomaly(reading);
        // System.out.println("Saving: " + reading); // Закоментуємо, якщо логи занадто великі
    }


    // Допоміжна функція для генерації значення з невеликим відхиленням від попереднього
    // БЕЗ ВЗАЄМОЗАЛЕЖНОСТІ - тільки коливання навколо попереднього значення
    private double generateValueWithinRange(SensorType type, double min, double max, double deviation) {
        double currentValue = currentSensorValues.get(type);
        // Додаємо випадкове відхилення від -deviation до +deviation
        double newValue = currentValue + (random.nextDouble() * 2 * deviation) - deviation;

        // Обмежуємо значення в межах діапазону
        return Math.max(min, Math.min(max, newValue));
    }


    // --- Функції генерації з взаємозалежностями ---

    private double generateLightIntensity() {
        // Діапазон: 10,000-70,000 люкс.
        // Додамо простий цикл день/ніч, щоб світло мало тренд
        int hour = LocalDateTime.now().getHour();
        double targetLight;
        if (hour >= 6 && hour < 10) { // Ранок, зростає
            targetLight = 10000 + (hour - 6) * (40000 / 4.0); // Від 10k до 50k
        } else if (hour >= 10 && hour < 18) { // День, високий рівень
            targetLight = 50000 + random.nextDouble() * 20000; // Від 50k до 70k
        } else if (hour >= 18 && hour < 22) { // Вечір, падає
            targetLight = 70000 - (hour - 18) * (60000 / 4.0); // Від 70k до 10k
        } else { // Ніч, низький рівень
            targetLight = 5000 + random.nextDouble() * 5000; // Від 5k до 10k (може бути майже 0, але для симуляції теплиці мінімум)
        }

        double currentLight = currentSensorValues.get(SensorType.LIGHT_INTENSITY);
        // Рухаємося до цільового значення з невеликим випадковим відхиленням
        double newValue = currentLight + (targetLight - currentLight) * 0.1 + (random.nextDouble() * 2 * LIGHT_DEVIATION) - LIGHT_DEVIATION;
        return Math.max(0, Math.min(70000, newValue)); // Обмежуємо діапазоном
    }


    private double generateTemperature() {
        // Температура залежить від інтенсивності світла
        double light = currentSensorValues.get(SensorType.LIGHT_INTENSITY);
        // Проста лінійна залежність: більше світла -> вища температура
        // Масштабуємо світло до діапазону 0-1
        double normalizedLight = light / 70000.0; // Макс 70000 люкс
        // Цільова температура буде вищою при більшому світлі
        double baseTemp = 18.0; // Базова температура
        double maxTempIncrease = 7.0; // Максимальне підвищення температури через світло (18 + 7 = 25)
        double targetTemp = baseTemp + (normalizedLight * maxTempIncrease); // 18-25
        if (light < 10000) { // Вночі або при дуже низькому світлі температура падає
            targetTemp = 15.0 + random.nextDouble() * 3.0; // 15-18
        }


        double currentValue = currentSensorValues.get(SensorType.AIR_TEMPERATURE);
        // Рухаємося до цільової температури + випадкове коливання
        double newValue = currentValue + (targetTemp - currentValue) * 0.1 + (random.nextDouble() * 2 * TEMP_DEVIATION) - TEMP_DEVIATION;

        // Обмежуємо діапазоном 15-30°C
        return Math.max(15.0, Math.min(30.0, newValue));
    }

    private double generateHumidity() {
        // Вологість повітря залежить від температури повітря (обернена кореляція)
        double airTemp = currentSensorValues.get(SensorType.AIR_TEMPERATURE);
        // Чим вища температура, тим нижча відносна вологість (при постійній абсолютній вологості)
        // Модель: при 15C - 70%, при 30C - 30% (лінійно)
        double targetHumidity;
        // normalizedTemp: 0 (15C) до 1 (30C)
        double normalizedTemp = (airTemp - 15.0) / 15.0; // Діапазон 15-30, тому 15
        targetHumidity = 70.0 - (normalizedTemp * 40.0); // 70 - 40 = 30
        if (targetHumidity < 30) targetHumidity = 30; // Обмеження

        double currentValue = currentSensorValues.get(SensorType.AIR_HUMIDITY);
        double newValue = currentValue + (targetHumidity - currentValue) * 0.1 + (random.nextDouble() * 2 * HUMIDITY_DEVIATION) - HUMIDITY_DEVIATION;
        return Math.max(30.0, Math.min(90.0, newValue));
    }

    private double generateCo2Level() {
        // CO2 залежить від світла та температури (більше світла/температури -> більше фотосинтезу -> менше CO2)
        double light = currentSensorValues.get(SensorType.LIGHT_INTENSITY);
        double airTemp = currentSensorValues.get(SensorType.AIR_TEMPERATURE);

        double baseCo2 = 600.0; // Базовий рівень CO2
        double reductionDueToLight = (light / 70000.0) * 200.0; // До 200 ppm зменшення при високому світлі
        double reductionDueToTemp = ((airTemp - 15.0) / 15.0) * 100.0; // До 100 ppm зменшення при високій температурі

        double targetCo2 = baseCo2 - reductionDueToLight - reductionDueToTemp;

        // CO2 може зростати вночі, коли фотосинтез не відбувається, або якщо є штучна подача
        if (light < 5000) { // Ніч або дуже низьке світло
            targetCo2 = baseCo2 + random.nextDouble() * 50; // CO2 може трохи зростати без споживання
        }

        double currentValue = currentSensorValues.get(SensorType.CO2_LEVEL);
        double newValue = currentValue + (targetCo2 - currentValue) * 0.1 + (random.nextDouble() * 2 * CO2_DEVIATION) - CO2_DEVIATION;
        return Math.max(300.0, Math.min(800.0, newValue));
    }

    private double generateSoilMoisture() {
        // Вологість ґрунту залежить від температури повітря (вища температура -> швидше висихання)
        // та від вологості повітря (нижча вологість повітря -> швидше випаровування з ґрунту)
        double airTemp = currentSensorValues.get(SensorType.AIR_TEMPERATURE);
        double airHumidity = currentSensorValues.get(SensorType.AIR_HUMIDITY);

        double targetMoisture = 50.0; // Середня вологість

        // Зменшення вологості ґрунту при високій температурі та низькій вологості повітря
        targetMoisture -= (airTemp - 15.0) * 0.5; // Кожна 1C понад 15 зменшує на 0.5%
        targetMoisture -= (70.0 - airHumidity) * 0.2; // Кожна 1% вологості нижче 70 зменшує на 0.2%

        // Якщо вологість стає занадто низькою, можна симулювати "полив" або просто не дати їй опуститися надто низько
        if (targetMoisture < 20 && random.nextDouble() < 0.1) { // 10% шанс "поливу" при низькій вологості
            targetMoisture = 70.0 + random.nextDouble() * 10; // Підвищити до 70-80%
        }

        double currentValue = currentSensorValues.get(SensorType.SOIL_MOISTURE);
        double newValue = currentValue + (targetMoisture - currentValue) * 0.1 + (random.nextDouble() * 2 * SOIL_MOISTURE_DEVIATION) - SOIL_MOISTURE_DEVIATION;
        return Math.max(10.0, Math.min(100.0, newValue));
    }

    private double generateSoilPh() {
        // pH ґрунту: в основному стабільний, але може трохи коливатися
        // Можна зробити дуже слабку залежність від вологості ґрунту (наприклад, дуже низька вологість може трохи підвищити pH)
        double soilMoisture = currentSensorValues.get(SensorType.SOIL_MOISTURE);

        double targetPh = 6.5; // Базовий pH

        // Дуже незначна залежність: якщо ґрунт дуже сухий (нижче 20%), pH може трохи зрости
        if (soilMoisture < 20) {
            targetPh += (20 - soilMoisture) * 0.01; // Наприклад, 0.01 за кожен відсоток нижче 20
        }

        double currentValue = currentSensorValues.get(SensorType.SOIL_PH);
        double newValue = currentValue + (targetPh - currentValue) * 0.05 + (random.nextDouble() * 2 * PH_DEVIATION) - PH_DEVIATION; // Менший коефіцієнт для плавності
        return Math.max(3.5, Math.min(9.0, newValue));
    }


}