package org.example.smartsensorproject.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Entity // Вказує, що це JPA-сутність (буде таблиця в БД)
@Table(name = "greenhouse_settings") // Назва таблиці в PostgreSQL
@Data // Lombok: автоматично генерує Getters, Setters, toString
public class GreenhouseSetting {
    @Id // Позначає первинний ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоінкремент в PostgreSQL
    private Long id;

    // Нам потрібно знати, до якого датчика застосовується налаштування
    private String sensorType;

    // Максимально допустиме значення для простого контролю
    private Double maxThreshold;

    // Мінімально допустиме значення для простого контролю
    private Double minThreshold;

    // *** ЦЕ КЛЮЧОВИЙ ПАРАМЕТР ДЛЯ Z-SCORE ***
    // Множник стандартного відхилення (наприклад, 3.0)
    private Double stdMultiplier;
}
