package org.example.smartsensorproject.service;

import org.example.smartsensorproject.model.GreenhouseSetting;
import org.example.smartsensorproject.model.SensorType;
import org.example.smartsensorproject.repository.GreenhouseSettingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {
    private final GreenhouseSettingRepository settingsRepository;

    public DataInitializer(GreenhouseSettingRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        // Перевіряємо, чи таблиця вже містить дані, щоб не створювати дублікати при кожному запуску
        if (settingsRepository.count() == 0) {

            // Створюємо базові налаштування для кожного типу сенсора
            Arrays.stream(SensorType.values()).forEach(type -> {
                GreenhouseSetting setting = new GreenhouseSetting();
                setting.setSensorType(type.name());
                setting.setStdMultiplier(3.0); // Встановлюємо 3.0 (3 сигми) за замовчуванням

                // Додаємо базові пороги для простого контролю
                switch (type) {
                    case AIR_TEMPERATURE -> { setting.setMinThreshold(15.0); setting.setMaxThreshold(30.0); }
                    case AIR_HUMIDITY -> { setting.setMinThreshold(30.0); setting.setMaxThreshold(90.0); }
                    case SOIL_MOISTURE -> { setting.setMinThreshold(40.0); setting.setMaxThreshold(80.0); }
                    case LIGHT_INTENSITY -> { setting.setMinThreshold(5000.0); setting.setMaxThreshold(70000.0); }
                    case SOIL_PH -> { setting.setMinThreshold(5.5); setting.setMaxThreshold(7.5); }
                    case CO2_LEVEL -> { setting.setMinThreshold(350.0); setting.setMaxThreshold(850.0); }
                }
                settingsRepository.save(setting);
            });
            System.out.println("✅ Ініціалізовано базові налаштування теплиці.");
        }
    }
}
