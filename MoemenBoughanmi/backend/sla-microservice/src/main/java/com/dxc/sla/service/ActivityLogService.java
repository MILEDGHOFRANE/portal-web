package com.dxc.sla.service;

import com.dxc.sla.entity.ActivityLog;
import com.dxc.sla.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public void log(Long userId, String userFullName, String action, String description, String entityType, Long entityId) {
        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setUserFullName(userFullName);
        log.setAction(action);
        log.setDescription(description);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        repository.save(log);
    }

    public List<ActivityLog> getRecent() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<ActivityLog> getByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
