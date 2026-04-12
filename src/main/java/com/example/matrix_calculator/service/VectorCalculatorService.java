package com.example.matrix_calculator.service;

import com.example.matrix_calculator.dto.CalculationSteps;
import com.example.matrix_calculator.dto.StepData;
import com.example.matrix_calculator.dto.VectorRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorCalculatorService {

    private CalculationSteps steps = new CalculationSteps();

    public List<StepData> getLastSteps() {
        return steps.getSteps();
    }

    public Object calculate(VectorRequest request) {
        steps.clear();
        steps.addStep("Начало вычислений над векторами");

        List<Double> a = request.getVectorA();
        List<Double> b = request.getVectorB();
        List<Double> c = request.getVectorC();
        double scalar = request.getScalar();

        return switch (request.getOperation()) {
            case "vectorAdd" -> handleAdd(a, b);
            case "vectorSub" -> handleSub(a, b);
            case "vectorMultiplyScalar" -> handleMultiplyScalar(a, scalar);
            case "vectorDot" -> handleDot(a, b);
            case "vectorCross" -> handleCross(a, b);
            case "vectorMixed" -> handleMixed(a, b, c);
            default -> throw new IllegalArgumentException("Неизвестная операция над векторами");
        };
    }

    private Object handleAdd(List<Double> a, List<Double> b) {
        validateSize(a, b);
        steps.addStep("Вектор A: " + vectorToString(a));
        steps.addStep("Вектор B: " + vectorToString(b));
        steps.addStep("Выполняем сложение: каждый компонент складывается с соответствующим компонентом");

        List<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) + b.get(i));
        }

        steps.addStep("Результат: " + vectorToString(result));
        return result;
    }

    private Object handleSub(List<Double> a, List<Double> b) {
        validateSize(a, b);
        steps.addStep("Вектор A: " + vectorToString(a));
        steps.addStep("Вектор B: " + vectorToString(b));
        steps.addStep("Выполняем вычитание: A[i] - B[i]");

        List<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) - b.get(i));
        }

        steps.addStep("Результат: " + vectorToString(result));
        return result;
    }

    private Object handleMultiplyScalar(List<Double> a, double scalar) {
        steps.addStep("Вектор A: " + vectorToString(a));
        steps.addStep("Умножаем на число: " + scalar);
        steps.addStep("Каждый компонент вектора умножается на число");

        List<Double> result = new ArrayList<>();
        for (double val : a) {
            result.add(val * scalar);
        }

        steps.addStep("Результат: " + vectorToString(result));
        return result;
    }

    private Object handleDot(List<Double> a, List<Double> b) {
        validateSize(a, b);
        steps.addStep("Вектор A: " + vectorToString(a));
        steps.addStep("Вектор B: " + vectorToString(b));
        steps.addStep("Скалярное произведение = A₁·B₁ + A₂·B₂ + ... + Aₙ·Bₙ");

        double sum = 0;
        StringBuilder calculation = new StringBuilder();
        for (int i = 0; i < a.size(); i++) {
            double product = a.get(i) * b.get(i);
            sum += product;
            if (i > 0) calculation.append(" + ");
            calculation.append(a.get(i)).append("·").append(b.get(i)).append("=").append(product);
        }
        steps.addStep("Вычисление: " + calculation.toString());
        steps.addStep("Скалярное произведение = " + sum);

        return sum;
    }

    private Object handleCross(List<Double> a, List<Double> b) {
        if (a.size() != 3 || b.size() != 3) {
            throw new IllegalArgumentException("Векторное произведение определено только для 3-мерных векторов");
        }

        steps.addStep("Вектор A: " + vectorToString(a));
        steps.addStep("Вектор B: " + vectorToString(b));
        steps.addStep("Векторное произведение = A × B");
        steps.addStep("Формула: (A₂B₃ - A₃B₂, A₃B₁ - A₁B₃, A₁B₂ - A₂B₁)");

        List<Double> result = new ArrayList<>();
        result.add(a.get(1) * b.get(2) - a.get(2) * b.get(1));
        result.add(a.get(2) * b.get(0) - a.get(0) * b.get(2));
        result.add(a.get(0) * b.get(1) - a.get(1) * b.get(0));

        steps.addStep("Результат: " + vectorToString(result));
        return result;
    }

    private Object handleMixed(List<Double> a, List<Double> b, List<Double> c) {
        if (a.size() != 3 || b.size() != 3 || c.size() != 3) {
            throw new IllegalArgumentException("Смешанное произведение определено только для 3-мерных векторов");
        }

        steps.addStep("Вектор A: " + vectorToString(a));
        steps.addStep("Вектор B: " + vectorToString(b));
        steps.addStep("Вектор C: " + vectorToString(c));
        steps.addStep("Смешанное произведение = A · (B × C)");
        steps.addStep("Вычисляем сначала векторное произведение B × C");

        List<Double> cross = new ArrayList<>();
        cross.add(b.get(1) * c.get(2) - b.get(2) * c.get(1));
        cross.add(b.get(2) * c.get(0) - b.get(0) * c.get(2));
        cross.add(b.get(0) * c.get(1) - b.get(1) * c.get(0));
        steps.addStep("B × C = " + vectorToString(cross));

        steps.addStep("Теперь скалярное произведение A · (B × C)");

        double result = a.get(0) * cross.get(0) + a.get(1) * cross.get(1) + a.get(2) * cross.get(2);
        steps.addStep("Смешанное произведение = " + result);

        return result;
    }

    private void validateSize(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Векторы должны быть одинаковой размерности");
        }
    }

    private String vectorToString(List<Double> v) {
        return v.stream()
                .map(val -> String.format("%.4f", val))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}