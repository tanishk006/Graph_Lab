package com.graphengine.api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of(
                "status", "up",
                "service", "GraphTheoryEngineAPI"
        );
    }
}
