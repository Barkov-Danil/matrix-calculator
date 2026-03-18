package com.example.matrix_calculator.dto;

public class StepData {
    private String text;
    private Object matrix;
    private String type;
    private int dividerColumn;

    public StepData(String text) {
        this.text = text;
        this.type = "text";
    }

    public StepData(String text, double[][] matrix) {
        this.text = text;
        this.matrix = matrix;
        this.type = "matrix";
    }

    public StepData(String text, double[][] matrix, boolean isAugmented) {
        this.text = text;
        this.matrix = matrix;
        this.type = "augmented";
        this.dividerColumn = matrix[0].length - 1;
    }

    public StepData(String text, double[] vector) {
        this.text = text;
        this.matrix = vector;
        this.type = "vector";
    }

    public String getText() { return text; }
    public Object getMatrix() { return matrix; }
    public String getType() { return type; }
    public int getDividerColumn() { return dividerColumn; }
}