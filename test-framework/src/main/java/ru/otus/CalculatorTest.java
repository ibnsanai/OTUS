package ru.otus;

import ru.otus.annotations.After;
import ru.otus.annotations.Before;
import ru.otus.annotations.Test;

public class CalculatorTest {

    private Calculator calculator;

    @Before
    public void setUp() {
        calculator = new Calculator();
    }

    @Test
    public void testAddition() {
        int result = calculator.add(2, 3);
        assert result == 5 : "Expected 5, but got " + result;
    }

    @Test
    public void testSubtraction() {
        int result = calculator.subtract(10, 4);
        assert result == 6 : "Expected 6, but got " + result;
    }

    @Test
    public void testMultiplication() {
        int result = calculator.multiply(6, 7);
        assert result == 42 : "Expected 42, but got " + result;
    }

    @Test
    public void testDivisionByZero() {
        calculator.divide(10, 0);
        assert false : "Expected ArithmeticException but no exception was thrown";
    }

    @After
    public void tearDown() {
        calculator = null;
    }
}
