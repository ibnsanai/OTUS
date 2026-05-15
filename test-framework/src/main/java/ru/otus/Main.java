package ru.otus;

import ru.otus.runner.TestRunner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Test Framework Demo");
        TestRunner.run(CalculatorTest.class);
        System.out.println("Framework execution completed");
    }
}
