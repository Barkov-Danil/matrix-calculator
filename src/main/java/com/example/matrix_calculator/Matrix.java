package com.example.matrix_calculator;

public class Matrix {
    private final int row;
    private final int column;
    private double data[][];
    private int swapCount = 0;

    public Matrix(int row, int column) {
        this.row = row;
        this.column = column;
        data = new double[row][column];
    }

    public Matrix(double[][] data) {
        this.row = data.length;
        this.column = data[0].length;
        this.data = new double[row][column];
        for (int i = 0; i < row; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, column);
        }
    }

    Matrix(Matrix matrix) {
        this.row = matrix.row;
        this.column = matrix.column;
        this.data = matrix.data.clone();
        this.swapCount = matrix.swapCount;
    }

    public Matrix add(Matrix matrix) {
        if (row != matrix.row || column != matrix.column) {
            throw new IllegalArgumentException("Матрицы разного размера");
        }
        Matrix result = new Matrix(row, column);
        for (int r = 0; r < row; r++) {
            for (int col = 0; col < column; col++) {
                result.data[r][col] = data[r][col] + matrix.data[r][col];
            }
        }
        return result;
    }

    public Matrix sub(Matrix matrix) {
        if (row != matrix.row || column != matrix.column) {
            throw new IllegalArgumentException("Матрицы разного размера");
        }
        Matrix result = new Matrix(row, column);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                result.data[i][j] = data[i][j] - matrix.data[i][j];
            }
        }
        return result;
    }

    public Matrix multi(double a) {
        Matrix result = new Matrix(row, column);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                result.data[i][j] = data[i][j] * a;
            }
        }
        return result;
    }

    public Matrix multi(Matrix matrix) {
        if (column != matrix.row) {
            throw new IllegalArgumentException(
                    "Число столбцов первой матрицы не равно числу строк второй");
        }
        Matrix result = new Matrix(row, matrix.column);
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < matrix.column; c++) {
                double cage = 0;
                for (int i = 0; i < matrix.row; i++) {
                    cage += data[r][i] * matrix.data[i][c];
                }
                result.data[r][c] = cage;
            }
        }
        return result;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(column, row);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                result.data[j][i] = data[i][j];
            }
        }
        return result;
    }

    public void set(int r, int c, double val) {
        data[r][c] = val;
    }

    public int getRow() { return row; }
    public int getColumn() { return column; }
    public double getVal(int row, int col) {
        System.out.println("getVal(" + row + "," + col + ") = " + data[row][col]);
        return data[row][col];
    }
    public double[][] getData() { return data; }

    private boolean isTriangularMatrix() {
        if (row != column) throw new IllegalArgumentException("Матрица не квадратная");
        for (int i = 0; i < row; i++) {
            for (int j = i + 1; j < column; j++) {
                if (Math.abs(data[i][j]) > 1e-10 || Math.abs(data[j][i]) > 1e-10) {
                    return false;
                }
            }
        }
        return true;
    }

    private double calculateDetTriangularMatrix() {
        double determinant = 1;
        for (int i = 0; i < row; i++) determinant *= data[i][i];
        return determinant * Math.pow(-1, swapCount);
    }

    private boolean hasZeroLine() {
        for (int i = 0; i < row; i++) {
            boolean allZero = true;
            for (int j = 0; j < column; j++) {
                if (Math.abs(data[i][j]) > 1e-10) { allZero = false; break; }
            }
            if (allZero) return true;
        }
        for (int i = 0; i < column; i++) {
            boolean allZero = true;
            for (int j = 0; j < row; j++) {
                if (Math.abs(data[j][i]) > 1e-10) { allZero = false; break; }
            }
            if (allZero) return true;
        }
        return false;
    }

    public Matrix minor(int i, int j) {
        Matrix minor = new Matrix(row - 1, column - 1);
        int minorRow = 0;
        for (int r = 0; r < row; r++) {
            if (r == i) continue;
            int minorCol = 0;
            for (int c = 0; c < column; c++) {
                if (c == j) continue;
                minor.data[minorRow][minorCol] = data[r][c];
                minorCol++;
            }
            minorRow++;
        }
        return minor;
    }

    public double det() {
        if (row != column) throw new IllegalArgumentException("Матрица не квадратная");
        if (row == 1) return data[0][0];
        if (row == 2) return data[0][0] * data[1][1] - data[0][1] * data[1][0];

        if (hasZeroLine()) return 0;
        if (isTriangularMatrix()) return calculateDetTriangularMatrix();

        Matrix copy = new Matrix(this);
        if (!copy.gauss()) return 0;

        double determinant = 1.0;
        for (int i = 0; i < row; i++) determinant *= copy.data[i][i];
        return (copy.swapCount % 2 == 0) ? determinant : -determinant;
    }

    private void swapRow(int i, int j) {
        double[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    private void findPivot(int i) {
        int maxRow = i;
        for (int r = i + 1; r < row; r++) {
            if (Math.abs(data[r][i]) > Math.abs(data[maxRow][i])) maxRow = r;
        }
        if (maxRow != i) {
            swapRow(i, maxRow);
            swapCount++;
        }
    }

    private boolean gauss() {
        int n = Math.min(row, column);
        for (int i = 0; i < n; i++) {
            findPivot(i);

            // Если pivot = 0, ищем ненулевой элемент в оставшихся строках
            if (Math.abs(data[i][i]) < 1e-10) {
                boolean found = false;
                for (int r = i + 1; r < row; r++) {
                    if (Math.abs(data[r][i]) > 1e-10) {
                        swapRow(i, r);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Весь столбец нулевой - пропускаем
                    continue;
                }
            }

            for (int r = i + 1; r < row; r++) {
                double factor = data[r][i] / data[i][i];
                if (Math.abs(factor) > 1e-10) {
                    for (int j = i; j < column; j++) {
                        data[r][j] -= factor * data[i][j];
                    }
                }
            }
        }
        return true;
    }

    public int rang() {
        Matrix copy = new Matrix(this);
        int n = Math.min(row, column);
        int rank = 0;

        for (int i = 0; i < n; i++) {
            // Ищем ненулевой элемент в столбце i начиная со строки rank
            int pivotRow = -1;
            for (int r = rank; r < row; r++) {
                if (Math.abs(copy.data[r][i]) > 1e-10) {
                    pivotRow = r;
                    break;
                }
            }

            if (pivotRow == -1) {
                continue; // Весь столбец нулевой
            }

            // Меняем строки местами
            if (pivotRow != rank) {
                copy.swapRow(rank, pivotRow);
            }

            // Обнуляем элементы ниже и ВЫШЕ в этом столбце (полное исключение)
            for (int r = 0; r < row; r++) {
                if (r != rank && Math.abs(copy.data[r][i]) > 1e-10) {
                    double factor = copy.data[r][i] / copy.data[rank][i];
                    for (int j = 0; j < column; j++) {
                        copy.data[r][j] -= factor * copy.data[rank][j];
                    }
                }
            }

            rank++;
        }

        return rank;
    }

    public Matrix inverse() {
        if (row != column) throw new IllegalArgumentException("Матрица должна быть квадратной");
        int n = row;
        double[][] augmented = new double[n][2 * n];

        for (int i = 0; i < n; i++) {
            System.arraycopy(data[i], 0, augmented[i], 0, n);
            augmented[i][n + i] = 1.0;
        }

        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(augmented[k][i]) > Math.abs(augmented[maxRow][i])) maxRow = k;
            }
            if (maxRow != i) {
                double[] temp = augmented[i];
                augmented[i] = augmented[maxRow];
                augmented[maxRow] = temp;
            }

            double pivot = augmented[i][i];
            if (Math.abs(pivot) < 1e-10) throw new IllegalArgumentException("Матрица вырождена");

            for (int j = 0; j < 2 * n; j++) augmented[i][j] /= pivot;

            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmented[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }

        Matrix inverse = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            System.arraycopy(augmented[i], n, inverse.data[i], 0, n);
        }
        return inverse;
    }

    public double detSimple() {
        if (row != column) throw new IllegalArgumentException("Матрица не квадратная");

        if (row == 1) return data[0][0];
        if (row == 2) return data[0][0] * data[1][1] - data[0][1] * data[1][0];
        if (row == 3) {
            // Формула для матрицы 3x3
            return data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1])
                    - data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0])
                    + data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0]);
        }

        // Для больших матриц используем метод Гаусса
        Matrix copy = new Matrix(this);
        if (!copy.gauss()) return 0;

        double det = 1.0;
        for (int i = 0; i < row; i++) {
            det *= copy.data[i][i];
        }
        return (copy.swapCount % 2 == 0) ? det : -det;
    }
}