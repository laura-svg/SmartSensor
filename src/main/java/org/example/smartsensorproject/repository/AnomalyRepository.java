package org.example.smartsensorproject.repository;

import org.example.smartsensorproject.model.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

}
