package com.dxc.sla.repository;

import com.dxc.sla.entity.AccountConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountConfigurationRepository extends JpaRepository<AccountConfiguration, Integer> {
    boolean existsByAccount(String account);
    List<AccountConfiguration> findByAccountContainingIgnoreCase(String account);
}
