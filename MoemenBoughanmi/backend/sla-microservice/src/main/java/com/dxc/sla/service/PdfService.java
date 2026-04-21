package com.dxc.sla.service;

import com.dxc.sla.entity.Request;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateRequestPdf(Request request) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        PdfWriter.getInstance(document, out);
        document.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(124, 58, 237));
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(80, 80, 80));
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font statusFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);

        // Header
        Paragraph title = new Paragraph("DXC Technology — Portail RH", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Document officiel de demande", new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Separator
        LineSeparator line = new LineSeparator(1, 100, new Color(124, 58, 237), Element.ALIGN_CENTER, -5);
        document.add(line);
        document.add(Chunk.NEWLINE);

        // Request number & status
        PdfPTable statusTable = new PdfPTable(2);
        statusTable.setWidthPercentage(100);
        statusTable.setSpacingBefore(10);
        statusTable.setSpacingAfter(15);

        PdfPCell numCell = new PdfPCell(new Phrase(request.getRequestNumber(), new Font(Font.HELVETICA, 14, Font.BOLD)));
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        statusTable.addCell(numCell);

        Color statusColor = switch (request.getStatus().toString()) {
            case "APPROVED" -> new Color(22, 163, 74);
            case "REJECTED" -> new Color(220, 38, 38);
            default -> new Color(217, 119, 6);
        };
        String statusLabel = switch (request.getStatus().toString()) {
            case "APPROVED" -> "APPROUVÉE";
            case "REJECTED" -> "REFUSÉE";
            default -> "EN ATTENTE";
        };
        PdfPCell statusCell = new PdfPCell(new Phrase(statusLabel, statusFont));
        statusCell.setBackgroundColor(statusColor);
        statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        statusCell.setPadding(6);
        statusCell.setBorder(Rectangle.NO_BORDER);
        statusTable.addCell(statusCell);
        document.add(statusTable);

        // Section: Request Info
        addSectionTitle(document, "Informations de la demande");
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);
        addRow(infoTable, "Type de demande", formatType(request.getType().toString()), labelFont, valueFont);
        addRow(infoTable, "Date de demande", request.getRequestDate() != null ? request.getRequestDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—", labelFont, valueFont);
        if (request.getStartDate() != null) addRow(infoTable, "Date début", request.getStartDate().format(DATE_FMT), labelFont, valueFont);
        if (request.getEndDate() != null) addRow(infoTable, "Date fin", request.getEndDate().format(DATE_FMT), labelFont, valueFont);
        if (request.getNumberOfDays() != null) addRow(infoTable, "Nombre de jours", request.getNumberOfDays() + " jour(s)", labelFont, valueFont);
        document.add(infoTable);

        // Section: Employee Info
        if (request.getEmployee() != null) {
            addSectionTitle(document, "Informations de l'employé");
            PdfPTable empTable = new PdfPTable(2);
            empTable.setWidthPercentage(100);
            empTable.setSpacingAfter(15);
            addRow(empTable, "Nom complet", request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName(), labelFont, valueFont);
            addRow(empTable, "Email", request.getEmployee().getEmail(), labelFont, valueFont);
            addRow(empTable, "Poste", request.getEmployee().getPosition() != null ? request.getEmployee().getPosition() : "—", labelFont, valueFont);
            addRow(empTable, "Département", request.getEmployee().getDepartment() != null ? request.getEmployee().getDepartment() : "—", labelFont, valueFont);
            document.add(empTable);
        }

        // Section: Motif
        addSectionTitle(document, "Motif de la demande");
        PdfPTable motifTable = new PdfPTable(1);
        motifTable.setWidthPercentage(100);
        motifTable.setSpacingAfter(15);
        PdfPCell motifCell = new PdfPCell(new Phrase(request.getMotif(), valueFont));
        motifCell.setPadding(10);
        motifCell.setBackgroundColor(new Color(255, 251, 235));
        motifTable.addCell(motifCell);
        document.add(motifTable);

        // Section: Approval info
        if (request.getApprovedBy() != null) {
            addSectionTitle(document, "Décision");
            PdfPTable approvalTable = new PdfPTable(2);
            approvalTable.setWidthPercentage(100);
            addRow(approvalTable, "Approuvé par", request.getApprovedBy(), labelFont, valueFont);
            if (request.getApprovedDate() != null)
                addRow(approvalTable, "Date d'approbation", request.getApprovedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), labelFont, valueFont);
            document.add(approvalTable);
        }
        if (request.getRejectedBy() != null) {
            addSectionTitle(document, "Décision");
            PdfPTable rejTable = new PdfPTable(2);
            rejTable.setWidthPercentage(100);
            addRow(rejTable, "Refusé par", request.getRejectedBy(), labelFont, valueFont);
            addRow(rejTable, "Motif du refus", request.getRejectionReason() != null ? request.getRejectionReason() : "—", labelFont, valueFont);
            document.add(rejTable);
        }

        // Footer
        document.add(Chunk.NEWLINE);
        document.add(new LineSeparator(1, 100, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -5));
        Paragraph footer = new Paragraph("Document généré automatiquement par le Portail RH DXC Technology", new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(8);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addSectionTitle(Document doc, String title) throws Exception {
        Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(124, 58, 237));
        Paragraph p = new Paragraph(title, sectionFont);
        p.setSpacingBefore(8);
        p.setSpacingAfter(6);
        doc.add(p);
        doc.add(new LineSeparator(0.5f, 100, new Color(220, 220, 220), Element.ALIGN_CENTER, -3));
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(new Color(235, 235, 235));
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new Color(248, 248, 248));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "—", valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(new Color(235, 235, 235));
        valueCell.setPadding(6);
        table.addCell(valueCell);
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
}
