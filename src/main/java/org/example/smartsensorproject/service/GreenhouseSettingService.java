package org.example.smartsensorproject.service;

import org.example.smartsensorproject.model.GreenhouseSetting;
import org.example.smartsensorproject.repository.GreenhouseSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GreenhouseSettingService {
@Autowired
private GreenhouseSettingRepository settingsRepository;

    // Метод для отримання множника Z-Score для конкретного сенсора
    public double getZScoreMultiplier(String sensorType) {
        // За замовчуванням повертаємо 3.0, якщо налаштувань немає
        return settingsRepository.findBySensorType(sensorType)
                .map(GreenhouseSetting::getStdMultiplier)
                .orElse(3.0);
    }

    public void updateSetting(GreenhouseSetting updatedSetting) {
        // 1. Знайти існуючий запис за sensorType
        settingsRepository.findBySensorType(updatedSetting.getSensorType())
                .ifPresentOrElse(existingSetting -> {
                            // 2. Оновити поля
                            existingSetting.setStdMultiplier(updatedSetting.getStdMultiplier());
                            existingSetting.setMinThreshold(updatedSetting.getMinThreshold());
                            existingSetting.setMaxThreshold(updatedSetting.getMaxThreshold());
                            // 3. Зберегти оновлений запис
                            settingsRepository.save(existingSetting);
                        },
                        () -> {
                            // Якщо запис не знайдено, зберегти як новий
                            settingsRepository.save(updatedSetting);
                        });
    }

    /**
     * Повертає список усіх налаштувань для відображення на сторінці керування.
     */
    public List<GreenhouseSetting> getAllSettings() {
        return settingsRepository.findAll();
    }

    /**
     * Зберігає або оновлює список налаштувань.
     * Використовується для пакетного оновлення даних з форми.
     */
    public void saveAllSettings(List<GreenhouseSetting> settings) {
        settingsRepository.saveAll(settings);
    }
    /**
     * Оновлює лише множник Z-Score для конкретного налаштування за його ID.
     * Це забезпечує інтерактивність.
     */
    public void updateStdMultiplier(Long id, double stdMultiplier) {
        settingsRepository.findById(id).ifPresent(setting -> {
            setting.setStdMultiplier(stdMultiplier);
            settingsRepository.save(setting);
        });
    }
}
