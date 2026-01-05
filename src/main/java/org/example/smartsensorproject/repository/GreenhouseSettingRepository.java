package org.example.smartsensorproject.repository;

import org.example.smartsensorproject.model.GreenhouseSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GreenhouseSettingRepository extends JpaRepository<GreenhouseSetting, Long>
{
    // Spring автоматично створює запит "SELECT * FROM ... WHERE sensor_type = :sensorType"
    Optional<GreenhouseSetting> findBySensorType(String sensorType);

}
