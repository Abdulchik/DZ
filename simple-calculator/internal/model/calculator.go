package model

import "fmt"

type Calculator struct {
	value float64
}

func NewCalculator(initialValue float64) *Calculator {
	return &Calculator{
		value: initialValue,
	}
}

func (c *Calculator) GetValue() float64 {
	return c.value
}

func (c *Calculator) Add(num float64) {
	c.value += num
}

func (c *Calculator) Subtract(num float64) {
	c.value -= num
}

func (c *Calculator) Multiply(num float64) {
	c.value *= num
}

func (c *Calculator) Divide(num float64) error {
	if num == 0 {
		return fmt.Errorf("деление на ноль")
	}
	c.value /= num
	return nil
}
