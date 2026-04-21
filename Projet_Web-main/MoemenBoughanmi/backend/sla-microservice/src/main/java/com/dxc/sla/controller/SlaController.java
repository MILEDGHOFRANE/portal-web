package com.dxc.sla.controller;

import com.dxc.sla.entity.SlaConfiguration;
import com.dxc.sla.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SlaController {

    private final SlaService slaService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllConfigurations() {
        List<SlaConfiguration> configs = slaService.getAllConfigurations();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", configs
        ));
    }

    @GetMapping("/{account}")
    public ResponseEntity<?> getConfiguration(@PathVariable String account) {
        try {
            SlaConfiguration config = slaService.getConfigurationByAccount(account);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", config
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> createConfiguration(@RequestBody SlaConfiguration config) {
        try {
            SlaConfiguration saved = slaService.createConfiguration(config);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuration SLA créée avec succès",
                "data", saved
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{account}")
    public ResponseEntity<?> updateConfiguration(
            @PathVariable String account,
            @RequestBody SlaConfiguration config) {
        try {
            SlaConfiguration updated = slaService.updateConfiguration(account, config);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuration mise à jour avec succès",
                "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{account}")
    public ResponseEntity<?> deleteConfiguration(@PathVariable String account) {
        try {
            slaService.deleteConfiguration(account);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuration supprimée"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
