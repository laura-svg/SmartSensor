package org.example.smartsensorproject.service;

import org.example.smartsensorproject.model.SensorReading;
import org.example.smartsensorproject.model.SensorType;
import org.example.smartsensorproject.repository.SensorReadingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorDataService {

    private final SensorReadingRepository sensorReadingRepository;

    public SensorDataService(SensorReadingRepository sensorReadingRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
    }

    public void saveSensorData(SensorReading sensorReading) {
        System.out.println("Saving: " + sensorReading);
        sensorReadingRepository.save(sensorReading);
    }

    public List<SensorReading> getSensorData(SensorType sensorType, LocalDateTime start, LocalDateTime end) {
        return sensorReadingRepository.findBySensorTypeAndTimestampBetween(sensorType, start, end);
    }
}