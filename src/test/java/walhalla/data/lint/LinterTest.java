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

import java.util.List;

import org.junit.jupiter.api.Test;

import walhalla.data.lint.Linter.Expanded;

public class LinterTest {

    @Test
    void expandOptional() {
        List<Expanded> texts = Linter.expandOptionals("[a][b][c]");
        assert texts.size() == 8;
        assert texts.get(0).text.equals("");
        assert texts.get(0).options.equals(List.of("", "", ""));
        assert texts.get(1).text.equals("a");
        assert texts.get(1).options.equals(List.of("a", "", ""));
        assert texts.get(2).text.equals("b");
        assert texts.get(2).options.equals(List.of("", "b", ""));
        assert texts.get(3).text.equals("ab");
        assert texts.get(3).options.equals(List.of("a", "b", ""));
        assert texts.get(4).text.equals("c");
        assert texts.get(4).options.equals(List.of("", "", "c"));
        assert texts.get(5).text.equals("ac");
        assert texts.get(5).options.equals(List.of("a", "", "c"));
        assert texts.get(6).text.equals("bc");
        assert texts.get(6).options.equals(List.of("", "b", "c"));
        assert texts.get(7).text.equals("abc");
        assert texts.get(7).options.equals(List.of("a", "b", "c"));

        assert Linter.expandOptionals("A[a]B[b]C[c]D").size() == 8;
    }

    @Test
    void expandOptionals() {
        List<Expanded> texts = Linter.expandOptionals("[a][b|c]");
        assert texts.size() == 6;
        assert texts.get(0).text.equals("");
        assert texts.get(0).options.equals(List.of("", ""));
        assert texts.get(1).text.equals("a");
        assert texts.get(1).options.equals(List.of("a", ""));
        assert texts.get(2).text.equals("b");
        assert texts.get(2).options.equals(List.of("", "b"));
        assert texts.get(3).text.equals("ab");
        assert texts.get(3).options.equals(List.of("a", "b"));
        assert texts.get(4).text.equals("c");
        assert texts.get(4).options.equals(List.of("", "c"));
        assert texts.get(5).text.equals("ac");
        assert texts.get(5).options.equals(List.of("a", "c"));

        texts = Linter.expandOptionals("[a|A][b|c]");
        assert texts.size() == 9;
        assert texts.get(0).text.equals("");
        assert texts.get(1).text.equals("a");
        assert texts.get(2).text.equals("A");
        assert texts.get(3).text.equals("b");
        assert texts.get(4).text.equals("ab");
        assert texts.get(5).text.equals("Ab");
        assert texts.get(6).text.equals("c");
        assert texts.get(7).text.equals("ac");
        assert texts.get(8).text.equals("Ac");
    }
}
