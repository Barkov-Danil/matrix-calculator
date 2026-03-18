package com.example.matrix_calculator.dto;

import java.util.List;

public class MatrixRequest {
    private String operation;
    private double scalar;
    private List<List<Double>> matrixA; // Матрица коэффициентов A
    private List<List<Double>> matrixB; // Матрица B (для операций с двумя матрицами)
    private List<Double> vectorB; // Вектор правых частей для СЛАУ
    private String slaeMethod; // Метод решения СЛАУ: "matrix", "cramer", "gauss"

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public double getScalar() { return scalar; }
    public void setScalar(double scalar) { this.scalar = scalar; }

    public List<List<Double>> getMatrixA() { return matrixA; }
    public void setMatrixA(List<List<Double>> matrixA) { this.matrixA = matrixA; }

    public List<List<Double>> getMatrixB() { return matrixB; }
    public void setMatrixB(List<List<Double>> matrixB) { this.matrixB = matrixB; }

    public List<Double> getVectorB() { return vectorB; }
    public void setVectorB(List<Double> vectorB) { this.vectorB = vectorB; }

    public String getSlaeMethod() { return slaeMethod; }
    public void setSlaeMethod(String slaeMethod) { this.slaeMethod = slaeMethod; }
}