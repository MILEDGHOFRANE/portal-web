package com.dxc.auth.repository;

import com.dxc.auth.entity.OtpCode;
import com.dxc.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findByUserAndCodeAndUsedFalse(User user, String code);
}
