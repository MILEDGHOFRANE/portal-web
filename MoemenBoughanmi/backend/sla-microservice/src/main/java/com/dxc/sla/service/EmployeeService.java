package com.dxc.sla.service;

import com.dxc.sla.entity.Employee;
import com.dxc.sla.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    public Employee createEmployee(Employee employee) {
        log.info("Création employé: {}", employee.getEmail());
        
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Un employé avec cet email existe déjà");
        }
        
        // Générer Employee ID
        if (employee.getEmployeeId() == null || employee.getEmployeeId().isEmpty()) {
            employee.setEmployeeId(generateEmployeeId());
        }
        
        // Initialiser les congés
        if (employee.getTotalLeaveDays() == null) {
            employee.setTotalLeaveDays(30);
        }
        if (employee.getUsedLeaveDays() == null) {
            employee.setUsedLeaveDays(0);
        }
        if (employee.getRemainingLeaveDays() == null) {
            employee.setRemainingLeaveDays(30);
        }
        
        return employeeRepository.save(employee);
    }
    
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
    }
    
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
    }
    
    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Employé non trouvé pour cet utilisateur"));
    }
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public Employee updateEmployee(Long id, Employee employeeData) {
        Employee employee = getEmployeeById(id);
        
        employee.setFirstName(employeeData.getFirstName());
        employee.setLastName(employeeData.getLastName());
        employee.setPosition(employeeData.getPosition());
        employee.setDepartment(employeeData.getDepartment());
        employee.setSalary(employeeData.getSalary());
        employee.setPhone(employeeData.getPhone());
        employee.setAddress(employeeData.getAddress());
        
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }
    
    public void updateLeaveDays(Long employeeId, int daysUsed) {
        Employee employee = getEmployeeById(employeeId);
        employee.setUsedLeaveDays(employee.getUsedLeaveDays() + daysUsed);
        employee.setRemainingLeaveDays(employee.getTotalLeaveDays() - employee.getUsedLeaveDays());
        employeeRepository.save(employee);
    }
    
    private String generateEmployeeId() {
        int year = LocalDate.now().getYear();
        long count = employeeRepository.count() + 1;
        return String.format("EMP-%d-%03d", year, count);
    }
}
