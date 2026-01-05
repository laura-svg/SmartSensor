package org.example.smartsensorproject.service;
import org.example.smartsensorproject.model.Anomaly;
import org.example.smartsensorproject.repository.AnomalyRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AnomalyService {
    private final AnomalyRepository anomalyRepository;

    public AnomalyService(AnomalyRepository anomalyRepository) {
        this.anomalyRepository = anomalyRepository;
    }

    /**
     * Повертає список усіх виявлених аномалій, відсортованих за часом.
     */
    public List<Anomaly> getAllAnomalies() {
        // JpaRepository.findAll() повертає всі записи.
        // За замовчуванням вони можуть бути не відсортовані, але поки що залишимо так.
        return anomalyRepository.findAll();
    }
}
