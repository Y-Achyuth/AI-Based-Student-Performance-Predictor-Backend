package com.spp.ml;

import com.spp.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ModelTrainer {

    private final Path modelFile;
    private final RiskPredictor riskPredictor;

    public ModelTrainer(@Value("${app.model.file}") String modelFile, RiskPredictor riskPredictor) {
        this.modelFile = Path.of(modelFile);
        this.riskPredictor = riskPredictor;
    }

    public void trainAndSaveModel(List<Student> students) throws IOException {
        double averageAttendance = students.stream()
                .mapToInt(Student::getAttendanceRate)
                .average()
                .orElse(75.0);
        double averagePerformance = students.stream()
                .mapToInt(Student::getPerformanceScore)
                .average()
                .orElse(65.0);

        Path parent = modelFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(modelFile, List.of(
                "BASELINE_ATTENDANCE=" + averageAttendance,
                "BASELINE_PERFORMANCE=" + averagePerformance));
        riskPredictor.updateBaselines(averageAttendance, averagePerformance);
    }
}
