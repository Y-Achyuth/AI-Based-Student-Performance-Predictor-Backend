package com.spp.dao;

import com.spp.ml.RiskPredictor;
import com.spp.model.Assessment;
import com.spp.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class StudentDAO {

    private static final String STUDENTS_SQL = """
            SELECT s.ROLL_NO, s.STUDENT_NAME, s.PHONE_NUMBER,
                   s.OVERALL_ATTENDANCE AS ATTENDANCE_RATE,
                   COALESCE(perf.PERF_SCORE, 0) AS PERFORMANCE_SCORE
            FROM STUDENTS s
            LEFT JOIN (
                SELECT ROLL_NO,
                       ROUND((SUM(SCORE) / NULLIF(SUM(MAX_SCORE), 0)) * 100) AS PERF_SCORE
                FROM ASSESSMENTS
                GROUP BY ROLL_NO
            ) perf ON s.ROLL_NO = perf.ROLL_NO
            ORDER BY s.ROLL_NO ASC
            """;

    private static final String ASSESSMENTS_SQL = """
            SELECT ASSESSMENT_TYPE, SUBJECT, SCORE, MAX_SCORE,
                   TO_CHAR(ASSESSMENT_DATE, 'YYYY-MM-DD') AS ASSESSMENT_DATE
            FROM ASSESSMENTS
            WHERE ROLL_NO = ?
            ORDER BY ASSESSMENT_DATE DESC
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RiskPredictor riskPredictor;

    public StudentDAO(JdbcTemplate jdbcTemplate, RiskPredictor riskPredictor) {
        this.jdbcTemplate = jdbcTemplate;
        this.riskPredictor = riskPredictor;
    }

    public List<Student> getAllStudents() {
        return jdbcTemplate.query(STUDENTS_SQL, (rs, rowNum) -> mapStudent(rs));
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getString("ROLL_NO"));
        student.setRollNo(rs.getString("ROLL_NO"));
        student.setName(rs.getString("STUDENT_NAME"));
        student.setPhoneNumber(rs.getString("PHONE_NUMBER"));
        student.setAttendanceRate(rs.getInt("ATTENDANCE_RATE"));
        student.setPerformanceScore(rs.getInt("PERFORMANCE_SCORE"));

        String prediction = riskPredictor.predictRisk(student);
        if ("High Risk".equals(prediction)) {
            student.setStatus("at-risk");
            student.setInterventionNeeded(true);
            student.getRiskFactors().add("ML Model detected critical performance or attendance levels");
        } else if ("Low Risk".equals(prediction)) {
            student.setStatus("excellent");
        } else {
            student.setStatus("average");
        }

        student.setTrend("stable");
        student.setLastUpdated(LocalDate.now().toString());
        student.setRecentAssessments(jdbcTemplate.query(ASSESSMENTS_SQL, ASSESSMENT_ROW_MAPPER, student.getRollNo()));
        return student;
    }

    private static final RowMapper<Assessment> ASSESSMENT_ROW_MAPPER = (rs, rowNum) -> {
        Assessment assessment = new Assessment();
        assessment.setType(rs.getString("ASSESSMENT_TYPE"));
        assessment.setSubject(rs.getString("SUBJECT"));
        assessment.setScore(rs.getDouble("SCORE"));
        assessment.setMaxScore(rs.getDouble("MAX_SCORE"));
        assessment.setDate(rs.getString("ASSESSMENT_DATE"));
        return assessment;
    };
}
