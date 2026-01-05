package org.example.smartsensorproject.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "anomalies") // Назва таблиці в PostgreSQL
@Data
public class Anomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Час виявлення аномалії
    private LocalDateTime timestamp;

    // Тип сенсора
    private String sensorType;

    // Значення, яке перевищило поріг
    private Double detectedValue;

    // Розрахована Z-оцінка
    private Double zScoreValue;

    // Тип аномалії (наприклад, 'Z-Score Violation')
    private String anomalyType;

    // Додаткова інформація
    private String description;
}
