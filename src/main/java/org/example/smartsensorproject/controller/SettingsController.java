package org.example.smartsensorproject.controller;

import org.springframework.stereotype.Controller;
import org.example.smartsensorproject.model.GreenhouseSetting;
import org.example.smartsensorproject.service.GreenhouseSettingService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Controller
public class SettingsController {
    private final GreenhouseSettingService settingService;

    public SettingsController(GreenhouseSettingService settingService) {
        this.settingService = settingService;
    }

    // Відображення форми з поточними налаштуваннями
    @GetMapping("/settings")
    public String showSettingsForm(Model model) {
        // Отримуємо всі записи налаштувань для відображення в таблиці
        List<GreenhouseSetting> settings = settingService.getAllSettings();
        model.addAttribute("settings", settings);
//        // Додаємо порожній об'єкт для POST-запиту (якщо будемо додавати новий, але поки оновлюємо існуючі)
//        model.addAttribute("newSetting", new GreenhouseSetting());
        return "settings";
    }

    // Обробка форми оновлення налаштувань
    @PostMapping("/settings/update/{id}")
    public String updateSetting(@PathVariable Long id,
                                @RequestParam("stdMultiplier") double stdMultiplier,
                                RedirectAttributes redirectAttributes) {

        // Викликаємо новий метод у сервісі, щоб оновити тільки множник
        settingService.updateStdMultiplier(id, stdMultiplier);

        redirectAttributes.addFlashAttribute("message", "✅ Чутливість аномалій успішно оновлено");
        return "redirect:/settings";
    }
}
