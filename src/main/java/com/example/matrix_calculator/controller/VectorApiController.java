package com.example.matrix_calculator.controller;

import com.example.matrix_calculator.dto.VectorRequest;
import com.example.matrix_calculator.service.VectorCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vector")
@CrossOrigin(origins = "*")
public class VectorApiController {

    @Autowired
    private VectorCalculatorService vectorCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody VectorRequest request) {
        try {
            Object result = vectorCalculatorService.calculate(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("steps", vectorCalculatorService.getLastSteps());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/operations")
    public ResponseEntity<?> getOperations() {
        Map<String, String> operations = new HashMap<>();
        operations.put("vectorAdd", "Сложение векторов");
        operations.put("vectorSub", "Вычитание векторов");
        operations.put("vectorMultiplyScalar", "Умножение вектора на число");
        operations.put("vectorDot", "Скалярное произведение");
        operations.put("vectorCross", "Векторное произведение");
        operations.put("vectorMixed", "Смешанное произведение");
        return ResponseEntity.ok(operations);
    }
}