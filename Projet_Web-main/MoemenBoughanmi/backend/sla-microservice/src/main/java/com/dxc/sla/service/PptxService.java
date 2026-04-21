package com.dxc.sla.service;

import com.dxc.sla.entity.Request;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PptxService {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color DXC_PURPLE  = new Color(124, 58, 237);
    private static final Color DXC_DARK    = new Color(15, 23, 42);
    private static final Color GRAY_LABEL  = new Color(100, 116, 139);
    private static final Color LIGHT_GRAY  = new Color(248, 250, 252);

    public byte[] generateRequestPptx(Request request) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.setPageSize(new Dimension(960, 540));

            buildTitleSlide(ppt, request);
            buildInfoSlide(ppt, request);
            if (request.getEmployee() != null) buildEmployeeSlide(ppt, request);
            if (request.getApprovedBy() != null || request.getRejectedBy() != null) buildDecisionSlide(ppt, request);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ppt.write(out);
            return out.toByteArray();
        }
    }

    // ── Slide 1: Title ──────────────────────────────────────────────────────
    private void buildTitleSlide(XMLSlideShow ppt, Request request) {
        XSLFSlide slide = ppt.createSlide();

        // Dark background
        XSLFAutoShape bg = slide.createAutoShape();
        bg.setAnchor(new Rectangle2D.Double(0, 0, 960, 540));
        bg.setFillColor(DXC_DARK);
        bg.setLineColor(DXC_DARK);

        // Purple accent bar
        XSLFAutoShape bar = slide.createAutoShape();
        bar.setAnchor(new Rectangle2D.Double(0, 0, 960, 8));
        bar.setFillColor(DXC_PURPLE);
        bar.setLineColor(DXC_PURPLE);

        addText(slide, "DXC Technology — Portail RH", 80, 120, 800, 70,
                32, true, Color.WHITE, TextParagraph.TextAlign.CENTER);
        addText(slide, "Document officiel de demande", 80, 195, 800, 40,
                16, false, new Color(148, 163, 184), TextParagraph.TextAlign.CENTER);

        // Request number
        addText(slide, request.getRequestNumber() != null ? request.getRequestNumber() : "—",
                80, 270, 800, 55, 26, true, new Color(167, 139, 250), TextParagraph.TextAlign.CENTER);

        // Status badge
        String statusLabel = switch (request.getStatus().toString()) {
            case "APPROVED" -> "✓  APPROUVÉE";
            case "REJECTED" -> "✗  REFUSÉE";
            default         -> "⏳  EN ATTENTE";
        };
        Color statusColor = switch (request.getStatus().toString()) {
            case "APPROVED" -> new Color(22, 163, 74);
            case "REJECTED" -> new Color(220, 38, 38);
            default         -> new Color(217, 119, 6);
        };

        XSLFAutoShape statusBg = slide.createAutoShape();
        statusBg.setAnchor(new Rectangle2D.Double(330, 345, 300, 50));
        statusBg.setFillColor(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 40));
        statusBg.setLineColor(statusColor);

        addText(slide, statusLabel, 330, 345, 300, 50, 16, true, statusColor, TextParagraph.TextAlign.CENTER);
    }

    // ── Slide 2: Request details ─────────────────────────────────────────────
    private void buildInfoSlide(XMLSlideShow ppt, Request request) {
        XSLFSlide slide = ppt.createSlide();
        addSlideBackground(slide);
        addSectionTitle(slide, "Informations de la demande", 20);

        int y = 90;
        y = addRow(slide, "Type de demande", formatType(request.getType().toString()), y);
        y = addRow(slide, "Date de soumission",
                request.getRequestDate() != null ? request.getRequestDate().format(DATETIME_FMT) : "—", y);
        if (request.getStartDate() != null)
            y = addRow(slide, "Date début", request.getStartDate().format(DATE_FMT), y);
        if (request.getEndDate() != null)
            y = addRow(slide, "Date fin", request.getEndDate().format(DATE_FMT), y);
        if (request.getNumberOfDays() != null)
            y = addRow(slide, "Nombre de jours", request.getNumberOfDays() + " jour(s)", y);

        y += 15;
        addSectionTitle(slide, "Motif", y);
        XSLFAutoShape motifBg = slide.createAutoShape();
        motifBg.setAnchor(new Rectangle2D.Double(40, y + 36, 880, 60));
        motifBg.setFillColor(new Color(255, 251, 235));
        motifBg.setLineColor(new Color(253, 230, 138));
        addText(slide, request.getMotif() != null ? request.getMotif() : "—",
                44, y + 36, 872, 60, 13, false, new Color(51, 65, 85), TextParagraph.TextAlign.LEFT);
    }

    // ── Slide 3: Employee info ───────────────────────────────────────────────
    private void buildEmployeeSlide(XMLSlideShow ppt, Request request) {
        XSLFSlide slide = ppt.createSlide();
        addSlideBackground(slide);
        addSectionTitle(slide, "Informations de l'employé", 20);

        var emp = request.getEmployee();
        int y = 90;
        y = addRow(slide, "Nom complet",  emp.getFirstName() + " " + emp.getLastName(), y);
        y = addRow(slide, "Email",        emp.getEmail() != null ? emp.getEmail() : "—", y);
        y = addRow(slide, "Poste",        emp.getPosition() != null ? emp.getPosition() : "—", y);
        y = addRow(slide, "Département",  emp.getDepartment() != null ? emp.getDepartment() : "—", y);
    }

    // ── Slide 4: Decision ────────────────────────────────────────────────────
    private void buildDecisionSlide(XMLSlideShow ppt, Request request) {
        XSLFSlide slide = ppt.createSlide();
        addSlideBackground(slide);
        addSectionTitle(slide, "Décision", 20);

        int y = 90;
        if (request.getApprovedBy() != null) {
            y = addRow(slide, "Approuvé par", request.getApprovedBy(), y);
            if (request.getApprovedDate() != null)
                y = addRow(slide, "Date d'approbation", request.getApprovedDate().format(DATETIME_FMT), y);
        }
        if (request.getRejectedBy() != null) {
            y = addRow(slide, "Refusé par", request.getRejectedBy(), y);
            if (request.getRejectionReason() != null)
                y = addRow(slide, "Motif du refus", request.getRejectionReason(), y);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addSlideBackground(XSLFSlide slide) {
        XSLFAutoShape bg = slide.createAutoShape();
        bg.setAnchor(new Rectangle2D.Double(0, 0, 960, 540));
        bg.setFillColor(Color.WHITE);
        bg.setLineColor(Color.WHITE);

        XSLFAutoShape bar = slide.createAutoShape();
        bar.setAnchor(new Rectangle2D.Double(0, 0, 960, 6));
        bar.setFillColor(DXC_PURPLE);
        bar.setLineColor(DXC_PURPLE);

        // Footer
        XSLFAutoShape footer = slide.createAutoShape();
        footer.setAnchor(new Rectangle2D.Double(0, 510, 960, 30));
        footer.setFillColor(LIGHT_GRAY);
        footer.setLineColor(LIGHT_GRAY);
        addText(slide, "Document généré automatiquement — Portail RH DXC Technology",
                0, 512, 960, 26, 9, false, GRAY_LABEL, TextParagraph.TextAlign.CENTER);
    }

    private void addSectionTitle(XSLFSlide slide, String title, int y) {
        addText(slide, title, 40, y, 880, 42, 18, true, DXC_PURPLE, TextParagraph.TextAlign.LEFT);
        XSLFAutoShape line = slide.createAutoShape();
        line.setAnchor(new Rectangle2D.Double(40, y + 38, 880, 2));
        line.setFillColor(new Color(233, 213, 255));
        line.setLineColor(new Color(233, 213, 255));
    }

    private int addRow(XSLFSlide slide, String label, String value, int y) {
        // Label background
        XSLFAutoShape lBg = slide.createAutoShape();
        lBg.setAnchor(new Rectangle2D.Double(40, y, 200, 34));
        lBg.setFillColor(LIGHT_GRAY);
        lBg.setLineColor(new Color(226, 232, 240));

        addText(slide, label, 44, y, 192, 34, 12, true, GRAY_LABEL, TextParagraph.TextAlign.LEFT);
        addText(slide, value != null ? value : "—", 248, y, 672, 34, 12, false, DXC_DARK, TextParagraph.TextAlign.LEFT);

        // Row separator
        XSLFAutoShape sep = slide.createAutoShape();
        sep.setAnchor(new Rectangle2D.Double(40, y + 33, 880, 1));
        sep.setFillColor(new Color(241, 245, 249));
        sep.setLineColor(new Color(241, 245, 249));

        return y + 36;
    }

    private void addText(XSLFSlide slide, String text, double x, double y, double w, double h,
                         int fontSize, boolean bold, Color color, TextParagraph.TextAlign align) {
        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle2D.Double(x, y, w, h));
        box.clearText();
        XSLFTextParagraph p = box.addNewTextParagraph();
        p.setTextAlign(align);
        XSLFTextRun run = p.addNewTextRun();
        run.setText(text);
        run.setFontSize((double) fontSize);
        run.setBold(bold);
        run.setFontColor(color);
    }

    private String formatType(String type) {
        return switch (type) {
            case "ATTESTATION_TRAVAIL"  -> "Attestation de travail";
            case "ATTESTATION_SALAIRE"  -> "Attestation de salaire";
            case "ATTESTATION_STAGE"    -> "Attestation de stage";
            case "ATTESTATION_EMPLOI"   -> "Attestation d'emploi";
            case "CONGE_ANNUEL"         -> "Congé annuel";
            case "CONGE_MALADIE"        -> "Congé maladie";
            case "CONGE_EXCEPTIONNEL"   -> "Congé exceptionnel";
            case "ASSURANCE"            -> "Assurance";
            default -> type;
        };
    }
}
