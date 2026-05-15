package ru.otus.runner;

import ru.otus.annotations.After;
import ru.otus.annotations.Before;
import ru.otus.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRunner {

    private record TestResult(
            String testName,
            boolean passed,
            Throwable exception,
            long duration) {
    }

    public static void run(Class<?> testClass) {
        long startTime = System.currentTimeMillis();

        List<Method> beforeMethods = getAnnotatedMethods(testClass, Before.class);
        List<Method> afterMethods = getAnnotatedMethods(testClass, After.class);
        List<Method> testMethods = getAnnotatedMethods(testClass, Test.class);

        if (testMethods.isEmpty()) {
            System.out.println("No @Test methods found in class " + testClass.getName());
            return;
        }

        List<TestResult> results = new ArrayList<>();
        for (Method testMethod : testMethods) {
            System.out.println("\n>>> Running test: " + testMethod.getName());
            TestResult result = runSingleTest(testClass, testMethod, beforeMethods, afterMethods);
            results.add(result);

            // Логируем результат теста
            if (result.passed()) {
                System.out.println("<<< Test PASSED (" + result.duration() + " ms)");
            } else {
                System.out.println("<<< Test FAILED (" + result.duration() + " ms)");
            }
        }

        printStatistics(results, startTime);
    }

    private static TestResult runSingleTest(Class<?> testClass, Method testMethod,
                                            List<Method> beforeMethods, List<Method> afterMethods) {
        long testStartTime = System.currentTimeMillis();

        // Создаем новый экземпляр для каждого теста (изоляция тестов)
        Object testInstance = createInstance(testClass);
        String testName = testMethod.getName();

        try {
            //Выполняем все @Before методы
            invokeMethods(testInstance, beforeMethods);

            //Выполняем сам тестовый метод
            testMethod.invoke(testInstance);

            //Выполняем все @After методы
            invokeMethods(testInstance, afterMethods);

            long duration = System.currentTimeMillis() - testStartTime;
            return new TestResult(testName, true, null, duration);

        } catch (Exception e) {
            // При возникновении исключения в тесте, всё равно пытаемся выполнить @After методы
            // Это важно для корректной очистки ресурсов
            try {
                invokeMethods(testInstance, afterMethods);
            } catch (Exception afterException) {
                System.err.println("  Error in @After methods for test '" + testName + "': " + afterException.getMessage());
            }

            // Извлекаем реальное исключение (reflection оборачивает исключения в InvocationTargetException)
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            long duration = System.currentTimeMillis() - testStartTime;
            return new TestResult(testName, false, cause, duration);
        }
    }

    private static Object createInstance(Class<?> testClass) {
        try {
            // Получаем конструктор без параметров
            Constructor<?> constructor = testClass.getDeclaredConstructor();
            // Делаем его доступным (на случай если он private)
            constructor.setAccessible(true);
            // Создаем новый экземпляр
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + testClass.getName(), e);
        }
    }

    private static void invokeMethods(Object target, List<Method> methods) {
        for (Method method : methods) {
            try {
                method.invoke(target);
            } catch (Exception e) {
                // Извлекаем реальную причину исключения
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw new RuntimeException("Failed to execute " + method.getName(), cause);
            }
        }
    }

    private static List<Method> getAnnotatedMethods(Class<?> testClass,
                                                    Class<? extends java.lang.annotation.Annotation> annotation) {
        return Arrays.stream(testClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .peek(method -> method.setAccessible(true))
                .toList();
    }

    private static void printStatistics(List<TestResult> results, long startTime) {
        long total = results.size();
        long passed = results.stream().filter(TestResult::passed).count();
        long failed = total - passed;

        long totalDuration = results.stream().mapToLong(TestResult::duration).sum();
        long overallDuration = System.currentTimeMillis() - startTime;

        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(70));
        System.out.printf("Total tests:  %d%n", total);
        System.out.printf("Passed:       %d (%d%%)%n", passed, (passed * 100 / total));
        System.out.printf("Failed:       %d (%d%%)%n", failed, (failed * 100 / total));
        System.out.println("-".repeat(70));
        System.out.printf("Total test time:  %d ms%n", totalDuration);
        System.out.printf("Overall runtime:  %d ms%n", overallDuration);
        System.out.println("=".repeat(70));

        // Выводим детали упавших тестов
        if (failed > 0) {
            System.out.println("\nFAILED TESTS DETAILS:");
            results.stream()
                    .filter(result -> !result.passed())
                    .forEach(result -> {
                        System.out.printf("  %s (execution time: %d ms)%n", result.testName(), result.duration());
                        if (result.exception() != null) {
                            System.out.printf("  Exception: %s%n", result.exception().getClass().getSimpleName());
                            System.out.printf("  Message: %s%n", result.exception().getMessage());
                            // Выводим стектрейс для отладки
                            result.exception().printStackTrace(System.out);
                        }
                        System.out.println();
                    });
        } else {
            System.out.println("\nAll tests passed successfully!");
        }

        System.out.println();
    }
}
