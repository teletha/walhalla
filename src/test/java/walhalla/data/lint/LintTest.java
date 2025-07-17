/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.data.lint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class LintTest {

    /**
     * Test case data holder
     */
    private static class TestCase {
        final String name;

        final String input;

        final String expected;

        TestCase(String name, String input, String expected) {
            this.name = name;
            this.input = input;
            this.expected = expected;
        }
    }

    /**
     * Creates dynamic test cases from descriptions.txt resource file.
     * The file format:
     * - Test case name (first line)
     * - Input data (second line)
     * - Expected output (third line and beyond)
     * - Cases are separated by "==="
     */
    @TestFactory
    Stream<DynamicTest> description() {
        List<TestCase> testCases = loadTestCases();

        return testCases.stream().map(data -> DynamicTest.dynamicTest(data.name, () -> {
            String fix = Proofreader.fix(data.input, data.name);
            assert fix.equals(data.expected) : String.format("\n入力値:\n%s\r\n\r\n期待値:\n%s\r\n\r\n実効値:\n%s", data.input, data.expected, fix);
        }));
    }

    /**
     * Loads test cases from the descriptions.txt resource file.
     */
    private List<TestCase> loadTestCases() {
        List<TestCase> testCases = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass()
                .getResourceAsStream("/descriptions.txt"), StandardCharsets.UTF_8))) {

            List<String> currentCase = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if ("===".equals(line.trim())) {
                    if (!currentCase.isEmpty()) {
                        testCases.add(parseTestCase(currentCase));
                        currentCase.clear();
                    }
                } else {
                    currentCase.add(line);
                }
            }

            // Handle the last test case if file doesn't end with ===
            if (!currentCase.isEmpty()) {
                testCases.add(parseTestCase(currentCase));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load test cases from descriptions.txt", e);
        }

        return testCases;
    }

    /**
     * Parses a single test case from lines.
     * Format:
     * - Line 0: Test case name
     * - Line 1: Input data
     * - Line 2+: Expected output (joined with newlines)
     */
    private TestCase parseTestCase(List<String> lines) {
        if (lines.size() < 3) {
            throw new IllegalArgumentException("Test case must have at least 3 lines: name, input, expected");
        }

        String name = lines.get(0);
        String input = lines.get(1);

        // Join lines 2+ as expected output
        StringBuilder expected = new StringBuilder();
        for (int i = 2; i < lines.size(); i++) {
            if (i > 2) {
                expected.append("\n");
            }
            expected.append(lines.get(i));
        }

        return new TestCase(name, input, expected.toString().trim());
    }
}