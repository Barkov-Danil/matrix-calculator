package com.example.matrix_calculator.dto;

import java.util.List;

public class MatrixRequest {
    private String operation;
    private double scalar;
    private List<List<Double>> matrixA;
    private List<List<Double>> matrixB;
    private List<Double> vectorB;
    private String slaeMethod;

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