package com.spp.ml;

import com.spp.model.Student;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class RiskPredictor {

    private volatile double baselineAttendance = 75.0;
    private volatile double baselinePerformance = 65.0;

    @Value("${app.model.file}")
    private String modelFile;

    @PostConstruct
    public void loadModel() throws IOException {
        Path path = Path.of(modelFile);
        if (!Files.exists(path)) {
            return;
        }

        double attendance = baselineAttendance;
        double performance = baselinePerformance;
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] entry = line.split("=", 2);
            if (entry.length != 2) {
                continue;
            }
            if ("BASELINE_ATTENDANCE".equals(entry[0])) {
                attendance = Double.parseDouble(entry[1]);
            } else if ("BASELINE_PERFORMANCE".equals(entry[0])) {
                performance = Double.parseDouble(entry[1]);
            }
        }
        updateBaselines(attendance, performance);
    }

    public String predictRisk(Student student) {
        double attendanceDistance = student.getAttendanceRate() - baselineAttendance;
        double performanceDistance = student.getPerformanceScore() - baselinePerformance;
        double riskWeight = (attendanceDistance * 0.4) + (performanceDistance * 0.6);

        if (riskWeight < -15 || student.getAttendanceRate() < 75 || student.getPerformanceScore() < 45) {
            return "High Risk";
        }
        if (riskWeight > 10 && student.getAttendanceRate() >= 80) {
            return "Low Risk";
        }
        return "Medium Risk";
    }

    public void updateBaselines(double attendance, double performance) {
        baselineAttendance = attendance;
        baselinePerformance = performance;
    }
}
