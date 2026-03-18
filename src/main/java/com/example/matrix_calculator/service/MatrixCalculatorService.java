package com.example.matrix_calculator.service;

import com.example.matrix_calculator.Matrix;
import com.example.matrix_calculator.dto.MatrixRequest;
import com.example.matrix_calculator.dto.CalculationSteps;
import com.example.matrix_calculator.dto.StepData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatrixCalculatorService {

    private CalculationSteps steps = new CalculationSteps();

    public Object calculate(MatrixRequest request) {
        steps.clear();
        steps.addStep("Начало вычислений");

        switch (request.getOperation()) {
            case "add":
            case "subtract":
            case "multiply":
                return handleBinaryOperation(request);

            case "multiplyScalar":
                return handleScalarMultiplication(request);

            case "transpose":
                return handleTranspose(request);

            case "determinant":
                return handleDeterminant(request);

            case "rank":
                return handleRank(request);

            case "inverse":
                return handleInverse(request);

            case "slae":
                return handleSlae(request);

            default:
                throw new IllegalArgumentException("Неизвестная операция");
        }
    }

    private Object handleBinaryOperation(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());
        Matrix b = createMatrix(request.getMatrixB());

        steps.addStep("Матрица A:", matrixToArray(a));
        steps.addStep("Матрица B:", matrixToArray(b));

        Matrix result;
        if ("add".equals(request.getOperation())) {
            steps.addStep("Выполняем сложение матриц");
            result = a.add(b);
        } else if ("subtract".equals(request.getOperation())) {
            steps.addStep("Выполняем вычитание матриц");
            result = a.sub(b);
        } else {
            steps.addStep("Выполняем умножение матриц");
            result = a.multi(b);
        }

        steps.addStep("Результат:", matrixToArray(result));
        return result.getData();
    }

    private Object handleScalarMultiplication(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());
        double scalar = request.getScalar();

        steps.addStep("Матрица A:", matrixToArray(a));
        steps.addStep("Умножаем на число: " + scalar);

        Matrix result = a.multi(scalar);
        steps.addStep("Результат:", matrixToArray(result));

        return result.getData();
    }

    private Object handleTranspose(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());

        steps.addStep("Исходная матрица:", matrixToArray(a));
        steps.addStep("Транспонируем");

        Matrix result = a.transpose();
        steps.addStep("Результат (транспонированная матрица):", matrixToArray(result));

        return result.getData();
    }

    private Object handleDeterminant(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());

        if (a.getRow() != a.getColumn()) {
            throw new IllegalArgumentException("Определитель можно вычислить только для квадратной матрицы");
        }

        steps.addStep("Матрица:", matrixToArray(a));
        steps.addStep("Вычисляем определитель");

        double det = a.det();
        steps.addStep("Определитель = " + det);

        return det;
    }

    private Object handleRank(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());

        steps.addStep("Матрица:", matrixToArray(a));
        steps.addStep("Вычисляем ранг матрицы");

        int rank = a.rang();
        steps.addStep("Ранг = " + rank);

        return rank;
    }

    private Object handleInverse(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());

        if (a.getRow() != a.getColumn()) {
            throw new IllegalArgumentException("Обратную матрицу можно найти только для квадратной матрицы");
        }

        steps.addStep("Матрица A:", matrixToArray(a));
        steps.addStep("Проверяем определитель");

        double det = a.det();
        if (Math.abs(det) < 1e-10) {
            throw new IllegalArgumentException("Матрица вырождена (определитель = 0), обратной матрицы не существует");
        }

        steps.addStep("Определитель = " + det + " ≠ 0, матрица невырождена");

        // Вычисляем матрицу алгебраических дополнений
        steps.addStep("Вычисляем матрицу алгебраических дополнений:");
        int n = a.getRow();
        double[][] cofactorMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Получаем минор
                Matrix minor = a.minor(i, j);
                double minorDet = minor.det();
                // Алгебраическое дополнение = (-1)^(i+j) * det(минор)
                double cofactor = ((i + j) % 2 == 0) ? minorDet : -minorDet;
                cofactorMatrix[i][j] = cofactor;
            }
        }

        steps.addStep("Матрица алгебраических дополнений:", cofactorMatrix);

        // Транспонируем матрицу алгебраических дополнений (получаем союзную матрицу)
        double[][] adjugateMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adjugateMatrix[i][j] = cofactorMatrix[j][i];
            }
        }

        steps.addStep("Союзная матрица (транспонированная матрица алгебраических дополнений):", adjugateMatrix);

        // Делим каждый элемент на определитель
        steps.addStep("Делим каждый элемент союзной матрицы на определитель det(A) = " + det);

        double[][] inverseData = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverseData[i][j] = adjugateMatrix[i][j] / det;
            }
        }

        Matrix result = new Matrix(inverseData);
        steps.addStep("Обратная матрица A⁻¹:", matrixToArray(result));

        return result.getData();
    }

    private Object handleSlae(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());
        List<Double> b = request.getVectorB();

        // ОТЛАДКА: выводим оригинальную матрицу
        System.out.println("=== ORIGINAL MATRIX IN SLAE ===");
        double[][] originalData = matrixToArray(a);
        for (int i = 0; i < originalData.length; i++) {
            for (int j = 0; j < originalData[i].length; j++) {
                System.out.print(originalData[i][j] + " ");
            }
            System.out.println();
        }

        if (a.getRow() != b.size()) {
            throw new IllegalArgumentException("Размер матрицы коэффициентов не соответствует размеру вектора правых частей");
        }

        steps.addStep("Решаем систему линейных уравнений");
        steps.addStep("Матрица коэффициентов A:", matrixToArray(a));
        steps.addStep("Вектор правых частей B:", vectorToArray(b));

        String method = request.getSlaeMethod();
        if (method == null) method = "gauss";

        String methodName;
        switch (method) {
            case "matrix": methodName = "Матричный метод (X = A⁻¹·B)"; break;
            case "cramer": methodName = "Метод Крамера"; break;
            case "gauss": methodName = "Метод Гаусса"; break;
            default: methodName = method;
        }
        steps.addStep("Метод решения: " + methodName);

        double[] solution;
        switch (method) {
            case "matrix":
                if (a.getRow() != a.getColumn()) {
                    throw new IllegalArgumentException("Матричный метод применим только для квадратных матриц");
                }
                solution = solveMatrixMethod(a, b);
                break;
            case "cramer":
                if (a.getRow() != a.getColumn()) {
                    throw new IllegalArgumentException("Метод Крамера применим только для квадратных матриц");
                }
                solution = solveCramerMethod(a, b);
                break;
            case "gauss":
                solution = solveGaussMethod(a, b);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный метод решения СЛАУ");
        }

        steps.addStep("Полученное решение:", solution);

        List<Double> result = new ArrayList<>();
        for (double v : solution) {
            result.add(v);
        }

        return result;
    }

    private double[] solveMatrixMethod(Matrix a, List<Double> b) {
        steps.addStep("Матричный метод: X = A⁻¹ × B");
        steps.addStep("Находим обратную матрицу A⁻¹");

        Matrix aInverse = a.inverse();
        steps.addStep("A⁻¹:", matrixToArray(aInverse));

        double[] result = new double[a.getRow()];
        for (int i = 0; i < a.getRow(); i++) {
            double sum = 0;
            for (int j = 0; j < a.getColumn(); j++) {
                sum += aInverse.getVal(i, j) * b.get(j);
            }
            result[i] = sum;
        }

        return result;
    }

    private double[] solveCramerMethod(Matrix a, List<Double> b) {
        int n = a.getRow();

        // Создаём глубокую копию матрицы, чтобы избежать изменений
        double[][] originalCopy = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                originalCopy[i][j] = a.getVal(i, j);
            }
        }

        double detA = a.det();
        steps.addStep("Метод Крамера: определитель основной матрицы det(A) = " + detA);

        if (Math.abs(detA) < 1e-10) {
            throw new IllegalArgumentException("Определитель системы равен 0. Метод Крамера не применим.");
        }

        double[] result = new double[n];

        for (int k = 0; k < n; k++) {
            steps.addStep("Находим определитель для x" + (k+1));

            // Используем копию, а не оригинальную матрицу
            double[][] akData = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (j == k) {
                        akData[i][j] = b.get(i);
                    } else {
                        akData[i][j] = originalCopy[i][j];
                    }
                }
            }

            System.out.println("Matrix A" + (k+1) + " (using copy):");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    System.out.print(akData[i][j] + " ");
                }
                System.out.println();
            }

            Matrix ak = new Matrix(akData);
            steps.addStep("Матрица A" + (k+1) + ":", akData);

            double detAk = ak.det();
            steps.addStep("det(A" + (k+1) + ") = " + detAk);

            result[k] = detAk / detA;
        }

        return result;
    }

    // Вспомогательный метод для печати матрицы
    private void printMatrix(Matrix m) {
        for (int i = 0; i < m.getRow(); i++) {
            for (int j = 0; j < m.getColumn(); j++) {
                System.out.print(m.getVal(i, j) + "\t");
            }
            System.out.println();
        }
    }

    private double[][] copyMatrix(double[][] original) {
        if (original == null) return null;
        double[][] copy = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    private double[] solveGaussMethod(Matrix a, List<Double> b) {
        int rows = a.getRow();
        int cols = a.getColumn();

        steps.addStep("Метод Гаусса: приводим расширенную матрицу к ступенчатому виду");
        steps.addStep("Размер системы: " + rows + " уравнений, " + cols + " неизвестных");

        // Создаём расширенную матрицу [A|B]
        double[][] augmented = new double[rows][cols + 1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                augmented[i][j] = a.getVal(i, j);
            }
            augmented[i][cols] = b.get(i);
        }

        steps.addStep("Расширенная матрица:", copyMatrix(augmented), true);

        // Прямой ход
        int rank = 0;
        for (int i = 0; i < Math.min(rows, cols); i++) {
            // Поиск главного элемента
            int maxRow = rank;
            for (int k = rank; k < rows; k++) {
                if (Math.abs(augmented[k][i]) > Math.abs(augmented[maxRow][i])) {
                    maxRow = k;
                }
            }

            if (Math.abs(augmented[maxRow][i]) < 1e-10) {
                continue;
            }

            if (maxRow != rank) {
                double[] temp = augmented[rank];
                augmented[rank] = augmented[maxRow];
                augmented[maxRow] = temp;
                steps.addStep("Переставляем строки " + (rank+1) + " и " + (maxRow+1) + ":", copyMatrix(augmented),
                        true);
            }

            double pivot = augmented[rank][i];
            for (int j = i; j <= cols; j++) {
                augmented[rank][j] /= pivot;
            }
            steps.addStep("Нормируем строку " + (rank+1) + ":", copyMatrix(augmented), true);

            for (int k = 0; k < rows; k++) {
                if (k != rank && Math.abs(augmented[k][i]) > 1e-10) {
                    double factor = augmented[k][i];
                    for (int j = i; j <= cols; j++) {
                        augmented[k][j] -= factor * augmented[rank][j];
                    }
                }
            }
            steps.addStep("После исключения переменной x" + (i+1) + ":", copyMatrix(augmented), true);

            rank++;
        }

        // Проверка на совместность
        for (int i = rank; i < rows; i++) {
            double sum = 0;
            for (int j = 0; j < cols; j++) {
                sum += Math.abs(augmented[i][j]);
            }
            if (sum < 1e-10 && Math.abs(augmented[i][cols]) > 1e-10) {
                throw new IllegalArgumentException("Система несовместна (решений нет)");
            }
        }

        if (rank < cols) {
            steps.addStep("Ранг матрицы = " + rank + ", число неизвестных = " + cols);
            steps.addStep("Система имеет бесконечно много решений. Найдём одно из них.");
        }

        // Обратный ход
        double[] solution = new double[cols];
        boolean[] isBasic = new boolean[cols];

        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                if (Math.abs(augmented[i][j] - 1.0) < 1e-10) {
                    boolean isBasicCol = true;
                    for (int k = 0; k < rows; k++) {
                        if (k != i && Math.abs(augmented[k][j]) > 1e-10) {
                            isBasicCol = false;
                            break;
                        }
                    }
                    if (isBasicCol) {
                        solution[j] = augmented[i][cols];
                        isBasic[j] = true;
                        break;
                    }
                }
            }
        }

        for (int j = 0; j < cols; j++) {
            if (!isBasic[j]) {
                solution[j] = 0;
            }
        }

        return solution;
    }

    private Matrix createMatrix(List<List<Double>> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Нет данных матрицы");
        }
        int rows = data.size();
        int cols = data.get(0).size();
        double[][] array = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = data.get(i).get(j);
            }
        }
        return new Matrix(array);
    }

    private String matrixToString(Matrix m) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < m.getRow(); i++) {
            if (i > 0) sb.append("; ");
            for (int j = 0; j < m.getColumn(); j++) {
                if (j > 0) sb.append(" ");
                sb.append(String.format("%.2f", m.getVal(i, j)));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String matrixToString(double[][] m) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < m.length; i++) {
            if (i > 0) sb.append("; ");
            for (int j = 0; j < m[i].length; j++) {
                if (j > 0) sb.append(" ");
                sb.append(String.format("%.2f", m[i][j]));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String vectorToString(List<Double> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < v.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.2f", v.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String vectorToString(double[] v) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.2f", v[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    private String getMethodName(String method) {
        switch (method) {
            case "matrix": return "Матричный метод";
            case "cramer": return "Метод Крамера";
            case "gauss": return "Метод Гаусса";
            default: return method;
        }
    }

    public List<StepData> getLastSteps() {
        return steps.getSteps();
    }

    private double[][] matrixToArray(Matrix m) {
        double[][] result = new double[m.getRow()][m.getColumn()];
        for (int i = 0; i < m.getRow(); i++) {
            for (int j = 0; j < m.getColumn(); j++) {
                result[i][j] = m.getVal(i, j);
            }
        }
        return result;
    }

    private double[] vectorToArray(List<Double> v) {
        double[] result = new double[v.size()];
        for (int i = 0; i < v.size(); i++) {
            result[i] = v.get(i);
        }
        return result;
    }

    private double[] vectorToArray(double[] v) {
        return v; // уже массив
    }
}