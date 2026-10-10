package com.spp.controller;

import com.spp.dao.AssessmentDAO;
import com.spp.model.Assessment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentDAO assessmentDAO;

    public AssessmentController(AssessmentDAO assessmentDAO) {
        this.assessmentDAO = assessmentDAO;
    }

    @PostMapping
    public Map<String, String> saveAssessment(@RequestBody AssessmentRequest request) {
        if (request.rollNo() == null || request.rollNo().isBlank()
                || request.assessmentType() == null || request.assessmentType().isBlank()
                || request.subject() == null || request.subject().isBlank()
                || request.score() < 0 || request.maxScore() <= 0 || request.score() > request.maxScore()) {
            throw new IllegalArgumentException("Invalid assessment payload");
        }

        Assessment assessment = new Assessment();
        assessment.setType(request.assessmentType());
        assessment.setSubject(request.subject());
        assessment.setScore(request.score());
        assessment.setMaxScore(request.maxScore());
        if (!assessmentDAO.saveAssessment(assessment, request.rollNo())) {
            throw new IllegalStateException("Database insertion failed");
        }
        return Map.of("status", "success");
    }

    public record AssessmentRequest(String rollNo, String assessmentType, String subject, double score,
                                    double maxScore) {
    }
}
