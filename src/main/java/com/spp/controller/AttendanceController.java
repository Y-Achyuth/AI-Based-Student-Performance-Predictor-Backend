package com.spp.controller;

import com.spp.dao.AttendanceDAO;
import com.spp.model.AttendanceRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceDAO attendanceDAO;

    public AttendanceController(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    @PostMapping
    public Map<String, String> saveAttendance(@RequestBody AttendanceRequest request) {
        if (request.date() == null || request.records() == null) {
            throw new IllegalArgumentException("Invalid attendance payload");
        }
        try {
            LocalDate.parse(request.date());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Attendance date must use YYYY-MM-DD format", exception);
        }
        for (AttendanceRecord record : request.records()) {
            if (record == null || record.getRollNo() == null || record.getRollNo().isBlank()
                    || record.getStatus() == null) {
                throw new IllegalArgumentException(
                        "Each attendance record requires a roll number and present/absent status");
            }
            String normalizedStatus = record.getStatus().toLowerCase();
            if (!List.of("present", "absent").contains(normalizedStatus)) {
                throw new IllegalArgumentException(
                        "Each attendance record requires a roll number and present/absent status");
            }
            record.setStatus(normalizedStatus);
        }

        attendanceDAO.saveAttendance(request.records(), request.date());
        return Map.of("status", "success");
    }

    public record AttendanceRequest(String date, List<AttendanceRecord> records) {
    }
}
