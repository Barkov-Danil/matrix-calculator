package com.example.matrix_calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// To do
// ---ступенчатый вид через алгоритм Гаусса
// ---метод смены строк
// улучшить нахождение определителя: добавить проверку свойств линейно зависимых строк
// нахождение обратной матрицы
// нахождение ранга матрицы
// улучшить гаусса
// добавить метод copy()
@SpringBootApplication
public class MatrixCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatrixCalculatorApplication.class, args);
	}

}
