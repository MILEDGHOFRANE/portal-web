package com.dxc.sla.controller;

import com.dxc.sla.entity.Employee;
import com.dxc.sla.entity.Request;
import com.dxc.sla.entity.RequestType;
import com.dxc.sla.service.EmployeeService;
import com.dxc.sla.service.PdfService;
import com.dxc.sla.service.PptxService;
import com.dxc.sla.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RequestController {

    private final RequestService requestService;
    private final EmployeeService employeeService;
    private final PdfService pdfService;
    private final PptxService pptxService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(@RequestBody Map<String, Object> body) {
        try {
            Map<?, ?> empMap = (Map<?, ?>) body.get("employee");
            Long employeeId = Long.valueOf(empMap.get("id").toString());

            Employee employee = employeeService.getEmployeeByUserId(employeeId);

            Request request = new Request();
            request.setEmployee(employee);
            request.setType(RequestType.valueOf(body.get("type").toString()));
            request.setMotif(body.get("motif").toString());

            if (body.get("startDate") != null && !body.get("startDate").toString().isEmpty()) {
                request.setStartDate(java.time.LocalDate.parse(body.get("startDate").toString()));
            }
            if (body.get("endDate") != null && !body.get("endDate").toString().isEmpty()) {
                request.setEndDate(java.time.LocalDate.parse(body.get("endDate").toString()));
            }
            if (body.get("transportType") != null && !body.get("transportType").toString().isEmpty()) {
                request.setTransportType(body.get("transportType").toString());
            }
            if (body.get("pickupAddress") != null && !body.get("pickupAddress").toString().isEmpty()) {
                request.setPickupAddress(body.get("pickupAddress").toString());
            }

            Request created = requestService.createRequest(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Demande envoyée avec succès",
                "request", created
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<?> getMyRequests(@PathVariable Long userId) {
        try {
            Employee employee = employeeService.getEmployeeByUserId(userId);
            List<Request> requests = requestService.getEmployeeRequests(employee.getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "requests", requests
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/hr/pending")
    public ResponseEntity<?> getHRPendingRequests() {
        try {
            List<Request> requests = requestService.getHRPendingRequests();
            return ResponseEntity.ok(Map.of("success", true, "requests", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/hr/all")
    public ResponseEntity<?> getAllHRRequests() {
        try {
            List<Request> requests = requestService.getAllHRRequests();
            return ResponseEntity.ok(Map.of("success", true, "requests", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/manager/pending")
    public ResponseEntity<?> getManagerPendingRequests() {
        try {
            List<Request> requests = requestService.getManagerPendingRequests();
            return ResponseEntity.ok(Map.of("success", true, "requests", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/manager/all")
    public ResponseEntity<?> getAllManagerRequests() {
        try {
            List<Request> requests = requestService.getAllManagerRequests();
            return ResponseEntity.ok(Map.of("success", true, "requests", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllRequests() {
        try {
            List<Request> requests = requestService.getAllRequests();
            return ResponseEntity.ok(Map.of("success", true, "requests", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String approverName = body.getOrDefault("approverName", "RH").toString();
            Long approverUserId = body.get("approverUserId") != null ? Long.valueOf(body.get("approverUserId").toString()) : null;
            Request request = requestService.approveRequest(id, approverName, approverUserId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Demande approuvée", "request", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String rejectorName = body.getOrDefault("rejectorName", "RH").toString();
            Long rejectorUserId = body.get("rejectorUserId") != null ? Long.valueOf(body.get("rejectorUserId").toString()) : null;
            String reason = body.getOrDefault("reason", "").toString();
            Request request = requestService.rejectRequest(id, rejectorName, rejectorUserId, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Demande refusée", "request", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequest(@PathVariable Long id) {
        try {
            Request request = requestService.getRequestById(id);
            return ResponseEntity.ok(Map.of("success", true, "request", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            Request request = requestService.getRequestById(id);
            byte[] pdf = pdfService.generateRequestPdf(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "demande_" + request.getRequestNumber() + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/pptx")
    public ResponseEntity<byte[]> downloadPptx(@PathVariable Long id) {
        try {
            Request request = requestService.getRequestById(id);
            byte[] pptx = pptxService.generateRequestPptx(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
            headers.setContentDispositionFormData("attachment", "demande_" + request.getRequestNumber() + ".pptx");
            return ResponseEntity.ok().headers(headers).body(pptx);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
