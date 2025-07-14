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

public class LinterTest {
    @Test
    void typo() {
        assert Proofreader.LINTER.fix("配置し、た瞬間").equals("配置した瞬間");
    }

    @Test
    void testName() {
        assertLine("15秒自身のＨＰが徐々に回復(毎秒40)", """
                15秒自身のHPが徐々に回復(毎秒40)
                スキル発動時にHPが最大値の50％回復
                """);

    }

    private boolean assertLine(String input, String expected) {
        String fixed = Proofreader.fix(input, null);
        List<String> split = List.of(fixed.split("\n"));
        List<String> expecteds = List.of(expected.trim().split("\r|\n|\r\n"));
        assert expecteds.size() == split.size();
        for (int i = 0; i < expecteds.size(); i++) {
            assert split.get(i).equals(expecteds.get(i));
        }
        return true;
    }
}
