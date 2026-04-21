package com.dxc.auth.repository;

import com.dxc.auth.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByResetTokenAndUsedFalse(String resetToken);
    Optional<PasswordReset> findByUserEmailAndUsedFalse(String userEmail);
    void deleteByUserEmail(String userEmail);
}
