package com.dxc.sla.controller;

import com.dxc.sla.entity.ActivityLog;
import com.dxc.sla.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<?> getActivity() {
        List<ActivityLog> logs = activityLogService.getRecent();
        return ResponseEntity.ok(Map.of("success", true, "logs", logs));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserActivity(@PathVariable Long userId) {
        List<ActivityLog> logs = activityLogService.getByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "logs", logs));
    }
}
