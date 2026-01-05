package org.example.smartsensorproject.repository;

import org.example.smartsensorproject.model.SensorReading;
import org.example.smartsensorproject.model.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findBySensorTypeAndTimestampBetween(SensorType sensorType, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM sensor_readings sr " +
            "WHERE sr.sensor_type = :sensorType " +
            "ORDER BY sr.timestamp DESC LIMIT :limit",
            nativeQuery = true)
    List<SensorReading> findLatestReadings(@Param("sensorType") String sensorType, @Param("limit") int limit);
}