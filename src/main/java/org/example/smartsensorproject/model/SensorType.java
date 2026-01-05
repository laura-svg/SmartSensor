package org.example.smartsensorproject.model;

import lombok.Getter;

@Getter
public enum SensorType {
    AIR_TEMPERATURE("Температура повітря"),
    AIR_HUMIDITY("Вологість повітря"),
    SOIL_MOISTURE("Вологість ґрунту"),
    LIGHT_INTENSITY("Інтенсивність світла"),
    SOIL_PH("Кислотність ґрунту (pH)"),
    CO2_LEVEL("Рівень вуглекислого газу");

    // Геттер для отримання української назви
    private final String displayName;

    // Конструктор
    SensorType(String displayName) {
        this.displayName = displayName;
    }

}