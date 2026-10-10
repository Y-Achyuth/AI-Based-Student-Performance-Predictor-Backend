package com.spp.controller;

import com.spp.dao.StudentDAO;
import com.spp.ml.ModelTrainer;
import com.spp.model.Student;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ml")
public class MLTrainingController {

    private final StudentDAO studentDAO;
    private final ModelTrainer modelTrainer;

    public MLTrainingController(StudentDAO studentDAO, ModelTrainer modelTrainer) {
        this.studentDAO = studentDAO;
        this.modelTrainer = modelTrainer;
    }

    @GetMapping("/train")
    public Map<String, String> trainModel() throws IOException {
        List<Student> students = studentDAO.getAllStudents();
        modelTrainer.trainAndSaveModel(students);
        return Map.of("status", "success", "message", "ML Model trained and saved successfully!");
    }
}
