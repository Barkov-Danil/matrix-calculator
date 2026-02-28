package com.example.matrix_calculator;

public class Matrix {
    private int row;
    private int column;
    private double data[][];
    private int swapCount = 0;

    Matrix(int row, int column) {
        this.row = row;
        this.column = column;
        data = new double[row][column];
    }

    Matrix add(Matrix matrix) {
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

    private void sub(int fromRow, int whatRow) {
        for (int c = 0; c < column; c++) {
            data[fromRow][c] -= data[whatRow][c];
        }
    }

    Matrix sub(Matrix matrix) {
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

    Matrix multi(double a) {
        Matrix result = new Matrix(row, column);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                result.data[i][j] = data[i][j] * a;
            }
        }
        return result;
    }

    private void multi(double a, int row) {
        for (int i = 0; i < column; i++) {
            data[row][i] *= a;
        }
    }

    Matrix multi(Matrix matrix) {
        if (column != matrix.row) {
            throw new IllegalArgumentException(
                    "The number of columns in the first matrix differs from the number of rows in the second matrix");
        }
        Matrix result = new Matrix(row, column);
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

    Matrix transpose(Matrix matrix) {
        Matrix result = new Matrix(column, row);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                result.data[j][i] = data[i][j];
            }
        }
        return result;
    }

    boolean isTriangularMatrix() {
        for (int i = 0; i < row; i++) {
            for (int j = i + 1; j < column; j++) {
                if (data[i][j] != 0 || data[j][i] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean hasZeroLine() {
        for (int i = 0; i < row; i++) {
            boolean flag = true;
            for (int j = 0; j < column; j++) {
                if (data[i][j] != 0) {
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
                if (data[j][i] != 0) {
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

    private double algebraicComplement(int i, int j) {
        Matrix complement = new Matrix(row - 1, column - 1);
        int correctR = 0;
        for (int r = 0; r < row; r++) {
            int correctC = 0;
            for (int c = 0; c < column; c++) {
                if (c == j) {
                    correctC = 1;
                    continue;
                }
                if (r == i) {
                    correctR = 1;
                    break;
                }
                complement.data[r - correctR][c - correctC] = data[r][c];
            }
        }
        if ((i + j) % 2 != 0) {
            for (int c = 0; c < column; c++) {
                complement.data[0][c] *= -1;
            }
        }
        return complement.det();
    }

    double det() {
        if (row != column) {
            throw new IllegalArgumentException("The matrix isn't square");
        }
        if (row == 1) return data[0][0];
        if (row == 2) return data[0][0] * data[1][1] - data[0][1] * data[1][0];
        double determinant = 1;
        Matrix copy = this.copy();
        if (copy.hasZeroLine()) {
            return 0;
        }
        if (copy.isTriangularMatrix()) {
            for (int i = 0; i < row; i++) {
                determinant *= copy.data[i][i];
            }
            return determinant * Math.pow(-1, swapCount);
        }
        copy.gauss();
        for (int i = 0; i < row; i++) {
            determinant *= copy.data[i][i];
        }
        return determinant * Math.pow(-1, swapCount);
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
        for (int i = 0; i < column; i++) {
            findPivot(i);
            multi(1 / data[i][i], i);
            for (int r = i + 1; r < row; r++) {
                multi(data[r][i], i);
                sub(r, i);
                multi(1 / data[r][i], i);
            }
        }
    }
}
