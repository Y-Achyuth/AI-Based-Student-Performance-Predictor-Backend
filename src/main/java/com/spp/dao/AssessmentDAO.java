package com.spp.dao;

import com.spp.model.Assessment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AssessmentDAO {

    private static final String INSERT_SQL = """
            INSERT INTO ASSESSMENTS (ROLL_NO, ASSESSMENT_TYPE, SUBJECT, SCORE, MAX_SCORE)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public AssessmentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean saveAssessment(Assessment assessment, String rollNo) {
        return jdbcTemplate.update(INSERT_SQL, rollNo, assessment.getType(), assessment.getSubject(),
                assessment.getScore(), assessment.getMaxScore()) > 0;
    }
}
