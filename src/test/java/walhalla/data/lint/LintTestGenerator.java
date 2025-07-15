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

import java.util.HashSet;
import java.util.Set;

import kiss.I;
import walhalla.data.Database;

public class LintTestGenerator {

    public static void main(String[] args) {
        Set<String> recorder = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        Linter.INTERCEPTOR = (desc, input, output) -> {
            if (recorder.add(desc) && !containsNonJapaneseLetters(desc)) {
                builder.append("    @Test\n");
                builder.append("    public void ").append(desc).append("() {\n");
                builder.append("        assertLine(\"").append(input).append("\", \"\"\"\n");
                for (String line : output.split("\n")) {
                    builder.append("            ").append(line).append("\n");
                }
                builder.append("            \"\"\");\n");
                builder.append("    }\n\n");
            }
        };

        Database db = I.make(Database.class);
        db.build();

        System.out.println(builder);
    }

    /**
     * 与えられた文字列に、漢字・ひらがな・カタカナ以外の文字が含まれているかを判定する。
     * 
     * @param input チェック対象の文字列
     * @return true：漢字・ひらがな・カタカナ以外が含まれている / false：すべて漢字・ひらがな・カタカナ
     */
    public static boolean containsNonJapaneseLetters(String input) {
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // サロゲートペア対応（補助漢字など）
            int codePoint = input.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++; // サロゲートペアなら次のcharも消費する
            }

            if (!isJapaneseChar(codePoint)) {
                return true;
            }

            if (input.contains("・")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 漢字・ひらがな・カタカナかどうかを判定する。
     */
    private static boolean isJapaneseChar(int codePoint) {
        return (codePoint >= 0x4E00 && codePoint <= 0x9FFF) // 漢字
                || (codePoint >= 0x3040 && codePoint <= 0x309F) // ひらがな
                || (codePoint >= 0x30A0 && codePoint <= 0x30FF) // カタカナ
                || (codePoint >= 0x31F0 && codePoint <= 0x31FF) // カタカナ拡張
                || (codePoint >= 0xFF66 && codePoint <= 0xFF9D); // 半角カナ（オプション）
    }
}
