package com.dxc.sla.repository;

import com.dxc.sla.entity.SlaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SlaRepository extends JpaRepository<SlaConfiguration, Long> {

    Optional<SlaConfiguration> findByAccount(String account);

    boolean existsByAccount(String account);

    void deleteByAccount(String account);
}
