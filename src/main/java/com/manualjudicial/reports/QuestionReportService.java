package com.manualjudicial.reports;

import com.manualjudicial.questions.Question;
import com.manualjudicial.questions.QuestionRepository;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionReportService {

    private final QuestionReportRepository reportRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;

    public QuestionReportDTO createReport(String userEmail, QuestionReportRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuestionReport report = new QuestionReport();
        report.setUser(user);
        report.setQuestion(question);
        report.setDescription(request.getDescription());
        report.setStatus(ReportStatus.PENDING);

        report = reportRepository.save(report);
        return QuestionReportDTO.fromEntity(report);
    }

    public List<QuestionReportDTO> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING)
                .stream().map(QuestionReportDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<QuestionReportDTO> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(QuestionReportDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public QuestionReportDTO resolveReport(Long id, ResolveReportRequest request) {
        QuestionReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setAdminNotes(request.getAdminNotes());
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        // Send email to user if requested
        if (request.getEmailResponse() != null && !request.getEmailResponse().trim().isEmpty()) {
            sendEmailToUser(report.getUser().getEmail(), request.getEmailResponse());
        }

        return QuestionReportDTO.fromEntity(report);
    }

    private void sendEmailToUser(String to, String messageText) {
        if (mailSender == null) {
            log.warn("Email configuration is missing. Cannot send answer to {}", to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Respuesta a tu reporte de pregunta - Manual Judicial");
            message.setText("Hola,\n\n" +
                    "Recientemente reportaste un posible error en una de nuestras preguntas. Nuestro equipo ha revisado tu reporte y nos gustaría informarte lo siguiente:\n\n" +
                    messageText + "\n\n" +
                    "Gracias por ayudarnos a mejorar el material de estudio.\n\n" +
                    "Atentamente,\nEl Equipo de Voy A Pasar");
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            // We do not throw to avoid crashing the transaction if the SMTP fails,
            // the report still gets saved properly, but we log the error.
        }
    }
}
