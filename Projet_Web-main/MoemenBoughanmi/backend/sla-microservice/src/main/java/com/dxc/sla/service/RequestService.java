package com.dxc.sla.service;

import com.dxc.sla.entity.*;
import com.dxc.sla.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final EmployeeService employeeService;
    private final EmailNotificationService emailNotificationService;
    private final ActivityLogService activityLogService;
    
    public Request createRequest(Request request) {
        log.info("Création demande: {} pour employé {}", request.getType(), request.getEmployee().getId());
        
        // Générer numéro de demande
        request.setRequestNumber(generateRequestNumber());
        request.setStatus(RequestStatus.PENDING);
        
        // Calculer nombre de jours pour congés
        if (request.getStartDate() != null && request.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            request.setNumberOfDays((int) days);
        }
        
        Request saved = requestRepository.save(request);
        emailNotificationService.sendSubmissionNotification(saved);
        String empName = request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName();
        activityLogService.log(request.getEmployee().getUserId(), empName, "REQUEST_SUBMITTED",
            empName + " a soumis une demande de " + formatTypeLabel(request.getType().toString()),
            "REQUEST", saved.getId());
        return saved;
    }
    
    public Request getRequestById(Long id) {
        return requestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
    }
    
    public List<Request> getEmployeeRequests(Long employeeId) {
        return requestRepository.findByEmployeeId(employeeId);
    }
    
    public List<Request> getPendingRequests() {
        return requestRepository.findByStatusOrderByRequestDateDesc(RequestStatus.PENDING);
    }
    
    private static final List<RequestType> HR_TYPES = Arrays.asList(
        RequestType.ATTESTATION_TRAVAIL,
        RequestType.ATTESTATION_SALAIRE,
        RequestType.ATTESTATION_STAGE,
        RequestType.ATTESTATION_EMPLOI,
        RequestType.ABONNEMENT_TRANSPORT
    );

    private static final List<RequestType> MANAGER_TYPES = Arrays.asList(
        RequestType.CONGE_ANNUEL,
        RequestType.CONGE_MALADIE,
        RequestType.CONGE_EXCEPTIONNEL,
        RequestType.ASSURANCE
    );

    public List<Request> getHRPendingRequests() {
        return requestRepository.findByTypeInAndStatusOrderByRequestDateDesc(HR_TYPES, RequestStatus.PENDING);
    }

    public List<Request> getAllHRRequests() {
        return requestRepository.findByTypeInOrderByRequestDateDesc(HR_TYPES);
    }

    public List<Request> getManagerPendingRequests() {
        return requestRepository.findByTypeInAndStatusOrderByRequestDateDesc(MANAGER_TYPES, RequestStatus.PENDING);
    }

    public List<Request> getAllManagerRequests() {
        return requestRepository.findByTypeInOrderByRequestDateDesc(MANAGER_TYPES);
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAllByOrderByRequestDateDesc();
    }
    
    public Request approveRequest(Long requestId, String approverName, Long approverUserId) {
        Request request = getRequestById(requestId);
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }
        
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(approverName);
        request.setApprovedByUserId(approverUserId);
        request.setApprovedDate(LocalDateTime.now());
        
        // Si c'est un congé, déduire les jours
        if (isLeaveRequest(request.getType()) && request.getNumberOfDays() != null) {
            employeeService.updateLeaveDays(request.getEmployee().getId(), request.getNumberOfDays());
        }
        
        log.info("Demande {} approuvée par {}", requestId, approverName);
        Request saved = requestRepository.save(request);
        emailNotificationService.sendApprovalEmail(saved);
        activityLogService.log(approverUserId, approverName, "REQUEST_APPROVED",
            approverName + " a approuvé la demande " + saved.getRequestNumber() + " de " + saved.getEmployee().getFirstName() + " " + saved.getEmployee().getLastName(),
            "REQUEST", requestId);
        return saved;
    }
    
    public Request rejectRequest(Long requestId, String rejectorName, Long rejectorUserId, String reason) {
        Request request = getRequestById(requestId);
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }
        
        request.setStatus(RequestStatus.REJECTED);
        request.setRejectedBy(rejectorName);
        request.setRejectedByUserId(rejectorUserId);
        request.setRejectedDate(LocalDateTime.now());
        request.setRejectionReason(reason);
        
        log.info("Demande {} rejetée par {}", requestId, rejectorName);
        Request saved = requestRepository.save(request);
        emailNotificationService.sendRejectionEmail(saved);
        activityLogService.log(rejectorUserId, rejectorName, "REQUEST_REJECTED",
            rejectorName + " a refusé la demande " + saved.getRequestNumber() + " de " + saved.getEmployee().getFirstName() + " " + saved.getEmployee().getLastName(),
            "REQUEST", requestId);
        return saved;
    }
    
    private String formatTypeLabel(String type) {
        return switch (type) {
            case "ATTESTATION_TRAVAIL" -> "attestation de travail";
            case "ATTESTATION_SALAIRE" -> "attestation de salaire";
            case "ATTESTATION_STAGE"   -> "attestation de stage";
            case "ATTESTATION_EMPLOI"  -> "attestation d'emploi";
            case "CONGE_ANNUEL"        -> "congé annuel";
            case "CONGE_MALADIE"       -> "congé maladie";
            case "CONGE_EXCEPTIONNEL"  -> "congé exceptionnel";
            case "ASSURANCE"           -> "assurance";
            case "ABONNEMENT_TRANSPORT" -> "abonnement transport";
            default -> type;
        };
    }

    private boolean isLeaveRequest(RequestType type) {
        return type == RequestType.CONGE_ANNUEL || 
               type == RequestType.CONGE_MALADIE || 
               type == RequestType.CONGE_EXCEPTIONNEL;
    }
    
    private String generateRequestNumber() {
        int year = LocalDate.now().getYear();
        long count = requestRepository.count() + 1;
        return String.format("REQ-%d-%04d", year, count);
    }
}
