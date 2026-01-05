package org.example.smartsensorproject.controller;
import org.example.smartsensorproject.model.Anomaly;
import org.example.smartsensorproject.service.AnomalyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@Controller
public class AnomalyController {
    private final AnomalyService anomalyService;

    public AnomalyController(AnomalyService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @GetMapping("/anomalies")
    public String showAnomalyLog(Model model) {
               // 2. Додаємо цей список до об'єкта Model, щоб він був доступний в HTML-шаблоні
        model.addAttribute("anomalies", anomalyService.getAllAnomalies());

        // 3. Повертаємо назву шаблону Thymeleaf
        return "anomalies";
    }
}
