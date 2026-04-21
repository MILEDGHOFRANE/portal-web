package com.dxc.sla.repository;

import com.dxc.sla.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeId(String employeeId);
    
    Optional<Employee> findByEmail(String email);
    
    Optional<Employee> findByUserId(Long userId);
    
    boolean existsByEmployeeId(String employeeId);
    
    boolean existsByEmail(String email);
}
