package com.example.matrix_calculator;

public class Matrix {
    private final int row;
    private final int column;
    private double data[][];
    private int swapCount = 0;

    Matrix(int row, int column) {
        this.row = row;
        this.column = column;
        data = new double[row][column];
    }

    Matrix(Matrix matrix) {
        this.row = matrix.row;
        this.column = matrix.column;
        this.data = matrix.data.clone();
        this.swapCount = matrix.swapCount;
    }

    public Matrix add(Matrix matrix) {
        if (row != matrix.row || column != matrix.column) {
            throw new IllegalArgumentException("The matrices have different sizes");
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
            throw new IllegalArgumentException("The matrices have different sizes");
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
                    "The number of columns in the first matrix differs from the number of rows in the second matrix");
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

    private boolean isTriangularMatrix() {
        if(row != column){
            throw new IllegalArgumentException("The matrix isn't square");
        }
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
        for (int i = 0; i < row; i++) {
            determinant *= data[i][i];
        }
        return determinant * Math.pow(-1, swapCount);
    }

    private boolean hasZeroLine() {
        for (int i = 0; i < row; i++) {
            boolean flag = true;
            for (int j = 0; j < column; j++) {
                if (Math.abs(data[i][j]) > 1e-10) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return true;
            }
        }
        for (int i = 0; i < column; i++) {
            boolean flag = true;
            for (int j = 0; j < row; j++) {
                if (Math.abs(data[j][i]) > 1e-10) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return true;
            }
        }
        return false;
    }

    private Matrix minor(int i, int j) {
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

    private double algebraicComplement(int i, int j) {
        return Math.pow(-1, i + j) * minor(i, j).det();
    }

    public double det() {
        if (row != column) {
            throw new IllegalArgumentException("The matrix isn't square");
        }
        if (row == 1) return data[0][0];
        if (row == 2) return data[0][0] * data[1][1] - data[0][1] * data[1][0];

        if (hasZeroLine()) {
            return 0;
        }
        if (isTriangularMatrix()) {
            return calculateDetTriangularMatrix();
        }
        double determinant = 1;
        Matrix copy = new Matrix(this);
        copy.gauss();
        for (int i = 0; i < row; i++) {
            determinant *= copy.data[i][i];
        }
        return determinant * Math.pow(-1, copy.swapCount);
    }

    private void swapRow(int i, int j) {
        double temp;
        for (int c = 0; c < column; c++) {
            temp = data[i][c];
            data[i][c] = data[j][c];
            data[j][c] = temp;
        }
    }

    private void findPivot(int i) {
        int numRowWithBiggestNumber = i;
        for (int c = i + 1; c < column; c++) {
            if (Math.abs(data[c][i]) > Math.abs(data[numRowWithBiggestNumber][i])) {
                numRowWithBiggestNumber = c;
            }
        }
        if (numRowWithBiggestNumber != i) {
            swapRow(i, numRowWithBiggestNumber);
            swapCount += 1;
        }
    }

    private void gauss() {
        for (int i = 0; i < row; i++) {
            findPivot(i);

            double divisor = data[i][i];
            if (Math.abs(divisor) < 1e-10) {
                continue;
            }

            for (int j = i; j < column; j++) {
                data[i][j] /= divisor;
            }

            for (int r = i + 1; r < row; r++) {
                double factor = data[r][i];
                if (Math.abs(factor) > 1e-10) {
                    for (int j = i; j < column; j++) {
                        data[r][j] -= factor * data[i][j];
                    }
                }
            }

        }
    }

    public int rang() {
        Matrix copy = new Matrix(this);
        copy.gauss();

        int rang = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (Math.abs(copy.data[i][j]) > 1e-10) {
                    rang++;
                    break;
                }
            }
        }
        return rang;
    }
}
