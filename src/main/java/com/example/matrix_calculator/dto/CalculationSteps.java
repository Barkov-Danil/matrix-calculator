package com.example.matrix_calculator.dto;

import java.util.ArrayList;
import java.util.List;

public class CalculationSteps {
    private List<StepData> steps = new ArrayList<>();

    public void addStep(String text) {
        steps.add(new StepData(text));
    }

    public void addStep(String text, double[][] matrix) {
        steps.add(new StepData(text, matrix));
    }

    public void addStep(String text, double[][] matrix, boolean isAugmented) {
        steps.add(new StepData(text, matrix, isAugmented));
    }

    public void addStep(String text, double[] vector) {
        steps.add(new StepData(text, vector));
    }

    public List<StepData> getSteps() {
        return steps;
    }

    public void clear() {
        steps.clear();
    }
}