package com.spp.controller;

import com.spp.dao.StudentDAO;
import com.spp.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final StudentDAO studentDAO;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.from-number}")
    private String fromNumber;

    public MessageController(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody MessageRequest request)
            throws IOException, InterruptedException {
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("Message body is required");
        }
        if (request.recipient() == null || request.recipient().isBlank()) {
            throw new IllegalArgumentException("Recipient group is required");
        }
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.<String, Object>of(
                            "status", "error",
                            "message", "Twilio credentials are not configured"));
        }

        List<Student> recipients = filterByGroup(studentDAO.getAllStudents(), request.recipient());
        int sent = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        for (Student student : recipients) {
            String phone = student.getPhoneNumber();
            if (phone == null || phone.isBlank()) {
                failed++;
                errors.add(student.getRollNo() + ": No phone number");
                continue;
            }
            try {
                if (sendTwilioSms(phone.trim(), request.message())) {
                    sent++;
                } else {
                    failed++;
                    errors.add(student.getRollNo() + ": Send failed");
                }
            } catch (IOException exception) {
                logger.warn("Twilio SMS failed for student {}", student.getRollNo(), exception);
                failed++;
                errors.add(student.getRollNo() + ": Send failed");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw exception;
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "sent", sent,
                "failed", failed,
                "total", recipients.size(),
                "errors", errors));
    }

    private List<Student> filterByGroup(List<Student> students, String group) {
        return switch (group) {
            case "at-risk" -> students.stream().filter(student -> "at-risk".equals(student.getStatus())).toList();
            case "low-attendance" -> students.stream().filter(student -> student.getAttendanceRate() < 75).toList();
            case "excellent" -> students.stream().filter(student -> "excellent".equals(student.getStatus())).toList();
            case "all" -> students;
            default -> throw new IllegalArgumentException("Unknown recipient group");
        };
    }

    private boolean sendTwilioSms(String toNumber, String message) throws IOException, InterruptedException {
        String endpoint = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String form = "To=" + encode(toNumber)
                + "&From=" + encode(fromNumber)
                + "&Body=" + encode(message);
        String credentials = Base64.getEncoder().encodeToString(
                (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        logger.info("Twilio SMS to {} returned HTTP {}", toNumber, response.statusCode());
        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record MessageRequest(String recipient, String message) {
    }
}
