package main

import (
	"fmt"
	"myproject/internal/model"
)

func main() {

	calc := model.NewCalculator(10)

	fmt.Printf("Начальное значение: %.2f\n", calc.GetValue())

	calc.Add(5)
	fmt.Printf("После +5: %.2f\n", calc.GetValue())

	calc.Subtract(3)
	fmt.Printf("После -3: %.2f\n", calc.GetValue())

	calc.Multiply(2)
	fmt.Printf("После *2: %.2f\n", calc.GetValue())

	err := calc.Divide(4)
	if err != nil {
		fmt.Println("Ошибка:", err)
	} else {
		fmt.Printf("После /4: %.2f\n", calc.GetValue())
	}

	err = calc.Divide(0)
	if err != nil {
		fmt.Println("Ошибка при делении на ноль:", err)
	}
}
