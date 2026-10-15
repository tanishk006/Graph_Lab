package algorithms;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== RUNNING JAVA JUNIT TESTS ===\n");
        Class<?> testClass = GraphAlgorithmsTest.class;
        int passed = 0;
        int failed = 0;
        List<String> failedTests = new ArrayList<>();

        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                String testName = method.getName();
                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();
                    method.invoke(instance);
                    System.out.println("[PASS] " + testName);
                    passed++;
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause();
                    System.err.println("[FAIL] " + testName + ": " + cause.getMessage());
                    failedTests.add(testName + " (" + cause.getClass().getSimpleName() + ": " + cause.getMessage() + ")");
                    failed++;
                } catch (Exception e) {
                    System.err.println("[ERROR] Could not run " + testName + ": " + e.getMessage());
                    failed++;
                }
            }
        }

        System.out.println("\n=== JAVA JUNIT SUMMARY: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.err.println("Failures:\n" + String.join("\n", failedTests));
            System.exit(1);
        }
    }
}
