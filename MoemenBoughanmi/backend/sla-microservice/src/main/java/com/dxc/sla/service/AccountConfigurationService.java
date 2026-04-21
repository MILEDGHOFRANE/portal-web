package com.dxc.sla.service;

import com.dxc.sla.entity.AccountConfiguration;
import com.dxc.sla.repository.AccountConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountConfigurationService {

    private final AccountConfigurationRepository repo;

    public List<AccountConfiguration> getAll() {
        return repo.findAll();
    }

    public AccountConfiguration getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
    }

    public List<AccountConfiguration> search(String name) {
        return repo.findByAccountContainingIgnoreCase(name);
    }

    public AccountConfiguration create(AccountConfiguration config) {
        if (repo.existsByAccount(config.getAccount())) {
            throw new RuntimeException("Un compte avec ce nom existe déjà");
        }
        log.info("Création compte: {}", config.getAccount());
        return repo.save(config);
    }

    public AccountConfiguration update(Integer id, AccountConfiguration incoming) {
        AccountConfiguration existing = getById(id);
        existing.setAccount(incoming.getAccount());
        existing.setDeskAccount(incoming.getDeskAccount());
        existing.setTimeFrame(incoming.getTimeFrame());
        existing.setTimeFrameOoh(incoming.getTimeFrameOoh());
        existing.setTimeFrameOther(incoming.getTimeFrameOther());
        existing.setAnswerSla(incoming.getAnswerSla());
        existing.setAbandonSla(incoming.getAbandonSla());
        existing.setOtherSla(incoming.getOtherSla());
        existing.setTargetAnswerRate(incoming.getTargetAnswerRate());
        existing.setTargetAbandonRate(incoming.getTargetAbandonRate());
        existing.setTargetOther(incoming.getTargetOther());
        log.info("Mise à jour compte: {}", existing.getAccount());
        return repo.save(existing);
    }

    public void delete(Integer id) {
        log.info("Suppression compte ID: {}", id);
        repo.deleteById(id);
    }
}
