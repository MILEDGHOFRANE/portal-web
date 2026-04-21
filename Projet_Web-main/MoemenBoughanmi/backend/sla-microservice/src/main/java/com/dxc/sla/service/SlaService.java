package com.dxc.sla.service;

import com.dxc.sla.entity.SlaConfiguration;
import com.dxc.sla.repository.SlaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaService {

    private final SlaRepository slaRepository;

    public List<SlaConfiguration> getAllConfigurations() {
        return slaRepository.findAll();
    }

    public SlaConfiguration getConfigurationByAccount(String account) {
        return slaRepository.findByAccount(account)
            .orElseThrow(() -> new RuntimeException("Configuration SLA non trouvée pour: " + account));
    }

    public SlaConfiguration createConfiguration(SlaConfiguration config) {
        log.info("Création configuration SLA pour account: {}", config.getAccount());
        if (slaRepository.existsByAccount(config.getAccount())) {
            throw new RuntimeException("Une configuration avec cet account existe déjà");
        }
        return slaRepository.save(config);
    }

    public SlaConfiguration updateConfiguration(String account, SlaConfiguration config) {
        SlaConfiguration existing = slaRepository.findByAccount(account)
            .orElseThrow(() -> new RuntimeException("Configuration SLA non trouvée pour: " + account));

        existing.setDeskAccount(config.getDeskAccount());
        existing.setTimeFrame(config.getTimeFrame());
        existing.setTimeFrameOoh(config.getTimeFrameOoh());
        existing.setTimeFrameOther(config.getTimeFrameOther());
        existing.setAnswerSla(config.getAnswerSla());
        existing.setAbandonSla(config.getAbandonSla());
        existing.setOtherSla(config.getOtherSla());
        existing.setTargetAnswerRate(config.getTargetAnswerRate());
        existing.setTargetAbandonRate(config.getTargetAbandonRate());
        existing.setTargetOther(config.getTargetOther());

        log.info("Mise à jour configuration SLA: {}", account);
        return slaRepository.save(existing);
    }

    @Transactional
    public void deleteConfiguration(String account) {
        log.info("Suppression configuration SLA: {}", account);
        slaRepository.deleteByAccount(account);
    }
}
