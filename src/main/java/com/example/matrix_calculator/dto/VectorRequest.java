package com.example.matrix_calculator.dto;

import java.util.List;

public class VectorRequest {
    private String operation;
    private double scalar;
    private List<Double> vectorA;
    private List<Double> vectorB;
    private List<Double> vectorC;

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public double getScalar() { return scalar; }
    public void setScalar(double scalar) { this.scalar = scalar; }

    public List<Double> getVectorA() { return vectorA; }
    public void setVectorA(List<Double> vectorA) { this.vectorA = vectorA; }

    public List<Double> getVectorB() { return vectorB; }
    public void setVectorB(List<Double> vectorB) { this.vectorB = vectorB; }

    public List<Double> getVectorC() { return vectorC; }
    public void setVectorC(List<Double> vectorC) { this.vectorC = vectorC; }
}