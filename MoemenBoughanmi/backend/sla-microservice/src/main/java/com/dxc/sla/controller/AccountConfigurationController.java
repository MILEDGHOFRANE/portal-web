package com.dxc.sla.controller;

import com.dxc.sla.entity.AccountConfiguration;
import com.dxc.sla.service.AccountConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountConfigurationController {

    private final AccountConfigurationService service;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(Map.of("success", true, "accounts", service.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("success", true, "account", service.getById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String name) {
        return ResponseEntity.ok(Map.of("success", true, "accounts", service.search(name)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AccountConfiguration config) {
        try {
            AccountConfiguration saved = service.create(config);
            return ResponseEntity.ok(Map.of("success", true, "message", "Compte créé avec succès", "account", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody AccountConfiguration config) {
        try {
            AccountConfiguration updated = service.update(id, config);
            return ResponseEntity.ok(Map.of("success", true, "message", "Compte mis à jour", "account", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Compte supprimé"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
