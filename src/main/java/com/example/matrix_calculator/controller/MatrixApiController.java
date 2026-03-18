package com.example.matrix_calculator.controller;

import com.example.matrix_calculator.Matrix;
import com.example.matrix_calculator.dto.MatrixRequest;
import com.example.matrix_calculator.service.MatrixCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MatrixApiController {

    @Autowired
    private MatrixCalculatorService calculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody MatrixRequest request) {
        try {
            Object result = calculatorService.calculate(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            if (calculatorService.getLastSteps() != null) {
                response.put("steps", calculatorService.getLastSteps());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/operations")
    public ResponseEntity<?> getOperations() {
        Map<String, String> operations = new HashMap<>();
        operations.put("add", "Сложение");
        operations.put("subtract", "Вычитание");
        operations.put("multiply", "Умножение матриц");
        operations.put("multiplyScalar", "Умножение на число");
        operations.put("transpose", "Транспонирование");
        operations.put("determinant", "Определитель");
        operations.put("rank", "Ранг");
        operations.put("inverse", "Обратная матрица");
        operations.put("slae", "Решение СЛАУ");
        return ResponseEntity.ok(operations);
    }

    @GetMapping("/slae-methods")
    public ResponseEntity<?> getSlaeMethods() {
        Map<String, String> methods = new HashMap<>();
        methods.put("matrix", "Матричный метод");
        methods.put("cramer", "Метод Крамера");
        methods.put("gauss", "Метод Гаусса");
        return ResponseEntity.ok(methods);
    }

    @Bean
    public CommandLineRunner testDeterminant() {
        return args -> {
            System.out.println("=== TEST DETERMINANT ===");
            double[][] data = {
                    {24, 3, 3},
                    {12, 5, -3},
                    {23, 23, 2}
            };
            Matrix m = new Matrix(data);
            double det = m.det();
            System.out.println("Determinant should be 2100, got: " + det);
        };
    }
}