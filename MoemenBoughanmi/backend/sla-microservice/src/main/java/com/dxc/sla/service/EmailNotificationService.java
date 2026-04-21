package com.dxc.sla.service;

import com.dxc.sla.entity.Request;
import com.dxc.sla.entity.RequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.hr.email}")
    private String hrEmail;

    // HR-only types (salary info is confidential)
    private static final java.util.Set<RequestType> HR_ONLY_TYPES = java.util.Set.of(
        RequestType.ATTESTATION_SALAIRE
    );

    // Types routed to HR
    private static final java.util.Set<RequestType> HR_TYPES = java.util.Set.of(
        RequestType.ATTESTATION_TRAVAIL,
        RequestType.ATTESTATION_SALAIRE,
        RequestType.ATTESTATION_STAGE,
        RequestType.ATTESTATION_EMPLOI
    );

    /**
     * Notifie RH ou Manager lorsqu'une nouvelle demande est soumise.
     * ATTESTATION_SALAIRE → RH uniquement (données salariales confidentielles).
     */
    public void sendSubmissionNotification(Request request) {
        try {
            String type = formatType(request.getType().toString());
            String empName = request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName();
            String reqNumber = request.getRequestNumber();
            boolean isHrType = HR_TYPES.contains(request.getType());
            boolean isSalaryType = HR_ONLY_TYPES.contains(request.getType());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(hrEmail);
            helper.setSubject("Nouvelle demande reçue — " + type + " (" + empName + ")");
            helper.setText(buildSubmissionHtml(empName, type, reqNumber, isSalaryType), true);

            mailSender.send(message);
            log.info("Notification soumission envoyée à RH pour demande {}", reqNumber);
        } catch (Exception e) {
            log.error("Erreur envoi notification soumission: {}", e.getMessage());
        }
    }

    /**
     * Email à l'employé quand sa demande est approuvée.
     * CC au RH pour archivage.
     */
    public void sendApprovalEmail(Request request) {
        try {
            String email = request.getEmployee().getEmail();
            String name = request.getEmployee().getFirstName();
            String type = formatType(request.getType().toString());
            String reqNumber = request.getRequestNumber();
            String approvedBy = request.getApprovedBy() != null ? request.getApprovedBy() : "l'équipe RH";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setCc(hrEmail); // copie RH
            helper.setSubject("Votre demande a été approuvée — " + type);
            helper.setText(buildApprovalHtml(name, type, reqNumber, approvedBy), true);

            mailSender.send(message);
            log.info("Email approbation envoyé à {} (CC: RH)", email);
        } catch (Exception e) {
            log.error("Erreur envoi email approbation: {}", e.getMessage());
        }
    }

    /**
     * Email à l'employé quand sa demande est refusée.
     * CC au RH pour archivage.
     */
    public void sendRejectionEmail(Request request) {
        try {
            String email = request.getEmployee().getEmail();
            String name = request.getEmployee().getFirstName();
            String type = formatType(request.getType().toString());
            String reqNumber = request.getRequestNumber();
            String rejectedBy = request.getRejectedBy() != null ? request.getRejectedBy() : "l'équipe RH";
            String reason = request.getRejectionReason() != null ? request.getRejectionReason() : "—";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setCc(hrEmail); // copie RH
            helper.setSubject("Votre demande a été refusée — " + type);
            helper.setText(buildRejectionHtml(name, type, reqNumber, rejectedBy, reason), true);

            mailSender.send(message);
            log.info("Email rejet envoyé à {} (CC: RH)", email);
        } catch (Exception e) {
            log.error("Erreur envoi email rejet: {}", e.getMessage());
        }
    }

    private String formatType(String type) {
        return switch (type) {
            case "ATTESTATION_TRAVAIL" -> "Attestation de travail";
            case "ATTESTATION_SALAIRE" -> "Attestation de salaire";
            case "ATTESTATION_STAGE" -> "Attestation de stage";
            case "ATTESTATION_EMPLOI" -> "Attestation d'emploi";
            case "CONGE_ANNUEL" -> "Congé annuel";
            case "CONGE_MALADIE" -> "Congé maladie";
            case "CONGE_EXCEPTIONNEL" -> "Congé exceptionnel";
            case "ASSURANCE" -> "Assurance";
            default -> type;
        };
    }

    private String buildSubmissionHtml(String empName, String type, String reqNumber, boolean isSalaryType) {
        String salaryNote = isSalaryType
            ? "<div style=\"background:#fffbeb;border:1px solid #fde68a;border-radius:8px;padding:14px;margin:16px 0\">"
            + "<p style=\"color:#92400e;font-size:13px;margin:0\">⚠️ <strong>Demande confidentielle</strong> — Cette demande concerne les informations salariales. Traitement RH exclusif.</p></div>"
            : "";
        return """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f9fafb;padding:32px;border-radius:12px">
                <div style="background:white;border-radius:10px;padding:32px;border:1px solid #e5e7eb">
                    <div style="text-align:center;margin-bottom:24px">
                        <div style="font-size:40px">📋</div>
                        <h1 style="font-size:20px;font-weight:700;color:#111827;margin:10px 0 4px">Nouvelle demande reçue</h1>
                        <p style="color:#6b7280;font-size:13px;margin:0">DXC Technology — Portail RH</p>
                    </div>
                    <p style="color:#374151;font-size:14px;margin-bottom:16px">Une nouvelle demande a été soumise et nécessite votre traitement.</p>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px">
                        <tr><td style="padding:8px 0;color:#6b7280;width:40%%">Employé</td><td style="padding:8px 0;font-weight:600;color:#111827">%s</td></tr>
                        <tr><td style="padding:8px 0;color:#6b7280">Type de demande</td><td style="padding:8px 0;font-weight:600;color:#111827">%s</td></tr>
                        <tr><td style="padding:8px 0;color:#6b7280">Référence</td><td style="padding:8px 0;font-family:monospace;background:#f3f4f6;padding:2px 6px;border-radius:4px">%s</td></tr>
                    </table>
                    %s
                    <div style="text-align:center;margin-top:20px">
                        <a href="http://localhost/html/hr-dashboard.html" style="background:#0f172a;color:white;padding:12px 24px;border-radius:8px;text-decoration:none;font-size:14px;font-weight:600">
                            Traiter la demande →
                        </a>
                    </div>
                    <p style="color:#6b7280;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #e5e7eb;margin-bottom:0">
                        DXC Technology — Notification automatique, merci de ne pas répondre.
                    </p>
                </div>
            </div>
            """.formatted(empName, type, reqNumber, salaryNote);
    }

    private String buildApprovalHtml(String name, String type, String reqNumber, String approvedBy) {
        return """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f9fafb;padding:32px;border-radius:12px">
                <div style="background:white;border-radius:10px;padding:32px;border:1px solid #e5e7eb">
                    <div style="text-align:center;margin-bottom:24px">
                        <div style="font-size:48px">✅</div>
                        <h1 style="font-size:22px;font-weight:700;color:#111827;margin:12px 0 4px">Demande approuvée</h1>
                        <p style="color:#6b7280;font-size:14px;margin:0">DXC Technology — Portail RH</p>
                    </div>
                    <p style="color:#374151;font-size:15px;margin-bottom:12px">Bonjour <strong>%s</strong>,</p>
                    <p style="color:#374151;font-size:14px;line-height:1.6">
                        Votre demande <strong>%s</strong>
                        (réf. <span style="font-family:monospace;background:#f3f4f6;padding:2px 6px;border-radius:4px">%s</span>)
                        a été <strong style="color:#16a34a">approuvée</strong> par <strong>%s</strong>.
                    </p>
                    <div style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:8px;padding:16px;margin:20px 0">
                        <p style="color:#16a34a;font-size:14px;margin:0">Vous pouvez télécharger votre document depuis le portail DXC.</p>
                    </div>
                    <p style="color:#6b7280;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #e5e7eb;margin-bottom:0">
                        DXC Technology — Message automatique, merci de ne pas y répondre.
                    </p>
                </div>
            </div>
            """.formatted(name, type, reqNumber, approvedBy);
    }

    private String buildRejectionHtml(String name, String type, String reqNumber, String rejectedBy, String reason) {
        return """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f9fafb;padding:32px;border-radius:12px">
                <div style="background:white;border-radius:10px;padding:32px;border:1px solid #e5e7eb">
                    <div style="text-align:center;margin-bottom:24px">
                        <div style="font-size:48px">❌</div>
                        <h1 style="font-size:22px;font-weight:700;color:#111827;margin:12px 0 4px">Demande refusée</h1>
                        <p style="color:#6b7280;font-size:14px;margin:0">DXC Technology — Portail RH</p>
                    </div>
                    <p style="color:#374151;font-size:15px;margin-bottom:12px">Bonjour <strong>%s</strong>,</p>
                    <p style="color:#374151;font-size:14px;line-height:1.6">
                        Votre demande <strong>%s</strong>
                        (réf. <span style="font-family:monospace;background:#f3f4f6;padding:2px 6px;border-radius:4px">%s</span>)
                        a été <strong style="color:#dc2626">refusée</strong> par <strong>%s</strong>.
                    </p>
                    <div style="background:#fef2f2;border:1px solid #fecaca;border-radius:8px;padding:16px;margin:20px 0">
                        <p style="color:#dc2626;font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px">Motif du refus</p>
                        <p style="color:#374151;font-size:14px;margin:0">%s</p>
                    </div>
                    <p style="color:#374151;font-size:14px;">Si vous pensez que cette décision est incorrecte, contactez votre responsable RH.</p>
                    <p style="color:#6b7280;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #e5e7eb;margin-bottom:0">
                        DXC Technology — Message automatique, merci de ne pas y répondre.
                    </p>
                </div>
            </div>
            """.formatted(name, type, reqNumber, rejectedBy, reason);
    }
}
