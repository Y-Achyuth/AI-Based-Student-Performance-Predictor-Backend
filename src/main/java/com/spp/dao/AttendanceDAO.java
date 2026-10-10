package com.spp.dao;

import com.spp.model.AttendanceRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AttendanceDAO {

    private static final String UPSERT_SQL = """
            INSERT INTO ATTENDANCE (ROLL_NO, ATTENDANCE_DATE, STATUS)
            VALUES (?, ?, ?)
            ON CONFLICT (ROLL_NO, ATTENDANCE_DATE)
            DO UPDATE SET STATUS = EXCLUDED.STATUS
            """;
    private static final String ABSENT_SQL =
            "UPDATE STUDENTS SET OVERALL_ATTENDANCE = GREATEST(OVERALL_ATTENDANCE - 3, 0) WHERE ROLL_NO = ?";
    private static final String PRESENT_SQL =
            "UPDATE STUDENTS SET OVERALL_ATTENDANCE = LEAST(OVERALL_ATTENDANCE + 1, 100) WHERE ROLL_NO = ? AND OVERALL_ATTENDANCE < 100";

    private final JdbcTemplate jdbcTemplate;

    public AttendanceDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public boolean saveAttendance(List<AttendanceRecord> records, String date) {
        Date attendanceDate = Date.valueOf(LocalDate.parse(date));
        for (AttendanceRecord record : records) {
            jdbcTemplate.update(UPSERT_SQL, record.getRollNo(), attendanceDate, record.getStatus());
            if ("absent".equalsIgnoreCase(record.getStatus())) {
                jdbcTemplate.update(ABSENT_SQL, record.getRollNo());
            } else if ("present".equalsIgnoreCase(record.getStatus())) {
                jdbcTemplate.update(PRESENT_SQL, record.getRollNo());
            }
        }
        return true;
    }
}
