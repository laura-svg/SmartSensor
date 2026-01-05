package org.example.smartsensorproject.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
@Data
public class SensorReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SensorType sensorType;

    private Double value;

    private LocalDateTime timestamp;

    @Column(name = "greenhouse_id")
    private String greenhouseId;
    }