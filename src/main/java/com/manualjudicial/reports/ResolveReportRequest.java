package com.manualjudicial.reports;

import lombok.Data;

@Data
public class ResolveReportRequest {
    private String adminNotes;
    private String emailResponse; // The message to send to the student
}
