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

        return switch (request.getOperation()) {
            case "add", "subtract", "multiply" -> handleBinaryOperation(request);
            case "multiplyScalar" -> handleScalarMultiplication(request);
            case "transpose" -> handleTranspose(request);
            case "determinant" -> handleDeterminant(request);
            case "rank" -> handleRank(request);
            case "inverse" -> handleInverse(request);
            case "slae" -> handleSlae(request);
            default -> throw new IllegalArgumentException("Неизвестная операция");
        };
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
        steps.addStep("Вычисляем определитель методом Гаусса");

        int n = a.getRow();
        double[][] working = matrixToArray(a);

        steps.addStep("Начальная матрица:", copyMatrix(working));

        int swapCount = 0;

        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(working[k][i]) > Math.abs(working[maxRow][i])) {
                    maxRow = k;
                }
            }

            if (Math.abs(working[maxRow][i]) < 1e-10) {
                steps.addStep("Обнаружен нулевой столбец, определитель = 0");
                return 0.0;
            }

            if (maxRow != i) {
                double[] temp = working[i];
                working[i] = working[maxRow];
                working[maxRow] = temp;
                swapCount++;
                steps.addStep("Переставляем строки " + (i+1) + " и " + (maxRow+1) + ":", copyMatrix(working));
            }

            for (int k = i + 1; k < n; k++) {
                double factor = working[k][i] / working[i][i];
                if (Math.abs(factor) > 1e-10) {
                    for (int j = i; j < n; j++) {
                        working[k][j] -= factor * working[i][j];
                    }
                }
            }

            boolean hasChanges = false;
            for (int k = i + 1; k < n; k++) {
                for (int j = i; j < n; j++) {
                    if (Math.abs(working[k][j]) > 1e-10) {
                        hasChanges = true;
                        break;
                    }
                }
            }
            if (hasChanges || i < n-1) {
                steps.addStep("После исключения переменной x" + (i+1) + ":", copyMatrix(working));
            }
        }

        double det = 1.0;
        for (int i = 0; i < n; i++) {
            det *= working[i][i];
        }
        if (swapCount % 2 != 0) {
            det = -det;
        }

        if (Math.abs(det) < 1e-10) {
            steps.addStep("Определитель = 0 (матрица вырождена)");
        } else {
            steps.addStep("Определитель = " + det);
        }

        return det;
    }

    private Object handleRank(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());

        steps.addStep("Матрица:", matrixToArray(a));
        steps.addStep("Вычисляем ранг матрицы методом Гаусса");

        int rows = a.getRow();
        int cols = a.getColumn();

        double[][] working = matrixToArray(a);
        steps.addStep("Начальная матрица:", copyMatrix(working));

        int rank = 0;
        int stepNumber = 1;

        for (int i = 0; i < Math.min(rows, cols); i++) {
            steps.addStep("Шаг " + stepNumber++ + ": рассматриваем столбец " + (i+1));

            int pivotRow = -1;
            for (int r = rank; r < rows; r++) {
                if (Math.abs(working[r][i]) > 1e-10) {
                    pivotRow = r;
                    break;
                }
            }

            if (pivotRow != -1) {
                steps.addStep("  Найден ненулевой элемент в строке " + (pivotRow+1) +
                        ", значение = " + working[pivotRow][i]);

                if (pivotRow != rank) {
                    double[] temp = working[rank];
                    working[rank] = working[pivotRow];
                    working[pivotRow] = temp;
                    steps.addStep("  Переставляем строки " + (rank+1) + " и " + (pivotRow+1) + ":",
                            copyMatrix(working));
                } else {
                    steps.addStep("  Элемент уже на нужной позиции");
                }

                double pivot = working[rank][i];
                steps.addStep("  Ведущий элемент = " + pivot);

                int zeroCount = 0;
                for (int r = 0; r < rows; r++) {
                    if (r != rank && Math.abs(working[r][i]) > 1e-10) {
                        double factor = working[r][i] / pivot;
                        for (int j = i; j < cols; j++) {
                            working[r][j] -= factor * working[rank][j];
                        }
                        zeroCount++;
                    }
                }

                if (zeroCount > 0) {
                    steps.addStep("  Обнуляем элементы в столбце " + (i+1) +
                            " в " + zeroCount + " других строках:", copyMatrix(working));
                } else {
                    steps.addStep("  В столбце " + (i+1) + " больше нет ненулевых элементов");
                }

                rank++;
                steps.addStep("  Текущий ранг = " + rank);
            } else {
                steps.addStep("  В столбце " + (i+1) + " нет ненулевых элементов, пропускаем");
            }
        }

        steps.addStep("Преобразованная матрица к ступенчатому виду:", copyMatrix(working));

        int nonZeroRows = 0;
        StringBuilder rankExplanation = new StringBuilder();
        rankExplanation.append("Находим количество линейно независимых строк:\n");

        for (int i = 0; i < rows; i++) {
            boolean isZeroRow = true;
            for (int j = 0; j < cols; j++) {
                if (Math.abs(working[i][j]) > 1e-10) {
                    isZeroRow = false;
                    break;
                }
            }
            if (!isZeroRow) {
                nonZeroRows++;
                rankExplanation.append("  Строка ").append(i+1).append(" содержит ненулевые элементы\n");
            } else {
                rankExplanation.append("  Строка ").append(i+1).append(" нулевая\n");
            }
        }

        steps.addStep(rankExplanation.toString());

        int finalRank = Math.min(rank, nonZeroRows);
        steps.addStep("Ранг матрицы = " + finalRank +
                " (количество линейно независимых строк/столбцов)");

        return finalRank;
    }

    private Object handleInverse(MatrixRequest request) {
        Matrix a = createMatrix(request.getMatrixA());

        Matrix original = new Matrix(a.getData());
        Matrix forDet = new Matrix(a.getData());

        if (original.getRow() != original.getColumn()) {
            throw new IllegalArgumentException("Обратную матрицу можно найти только для квадратной матрицы");
        }

        steps.addStep("Матрица A:", matrixToArray(original));
        steps.addStep("Проверяем определитель");

        double det = forDet.det();
        if (Math.abs(det) < 1e-10) {
            throw new IllegalArgumentException("Матрица вырождена (определитель = 0), обратной матрицы не существует");
        }

        steps.addStep("Определитель = " + det + " ≠ 0, матрица невырождена");

        steps.addStep("Вычисляем матрицу алгебраических дополнений:");
        int n = original.getRow();
        double[][] cofactorMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Matrix temp = new Matrix(original.getData());
                double cofactor = temp.algebraicComplement(i, j);
                cofactorMatrix[i][j] = cofactor;
            }
        }

        steps.addStep("Матрица алгебраических дополнений:", cofactorMatrix);

        double[][] adjugateMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adjugateMatrix[i][j] = cofactorMatrix[j][i];
            }
        }

        steps.addStep("Союзная матрица:", adjugateMatrix);

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

            Matrix ak = new Matrix(akData);
            steps.addStep("Матрица A" + (k+1) + ":", akData);

            double detAk = ak.det();
            steps.addStep("det(A" + (k+1) + ") = " + detAk);

            result[k] = detAk / detA;
        }

        return result;
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

        double[][] augmented = new double[rows][cols + 1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                augmented[i][j] = a.getVal(i, j);
            }
            augmented[i][cols] = b.get(i);
        }

        steps.addStep("Расширенная матрица:", copyMatrix(augmented), true);

        int rank = 0;
        for (int i = 0; i < Math.min(rows, cols); i++) {
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
}