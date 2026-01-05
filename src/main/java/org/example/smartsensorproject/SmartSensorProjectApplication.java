package org.example.smartsensorproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableScheduling
public class SmartSensorProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSensorProjectApplication.class, args);
    }

    @Controller
    public static class HomeController {
        @GetMapping("/")
        public String home() {
            return "main"; // повертаємо ім'я шаблону без .html
        }
    }
}
