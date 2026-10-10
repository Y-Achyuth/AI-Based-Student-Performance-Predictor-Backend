package com.spp.dao;

import com.spp.model.AttendanceRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class AttendanceDAO {

    private static final String MERGE_SQL = """
            MERGE INTO ATTENDANCE a
            USING (SELECT ? AS roll_no, TO_DATE(?, 'YYYY-MM-DD') AS att_date, ? AS status FROM dual) src
            ON (a.ROLL_NO = src.roll_no AND a.ATTENDANCE_DATE = src.att_date)
            WHEN MATCHED THEN UPDATE SET a.STATUS = src.status
            WHEN NOT MATCHED THEN INSERT (ROLL_NO, ATTENDANCE_DATE, STATUS)
                VALUES (src.roll_no, src.att_date, src.status)
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
        for (AttendanceRecord record : records) {
            jdbcTemplate.update(MERGE_SQL, record.getRollNo(), date, record.getStatus());
            if ("absent".equalsIgnoreCase(record.getStatus())) {
                jdbcTemplate.update(ABSENT_SQL, record.getRollNo());
            } else if ("present".equalsIgnoreCase(record.getStatus())) {
                jdbcTemplate.update(PRESENT_SQL, record.getRollNo());
            }
        }
        return true;
    }
}
