/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.data;

/**
 * Utility class for parsing text related to artist names and extracting clean artist names from various formats.
 */
public class TextParser {

    /**
     * Extracts a clean artist name from a given text string.
     * <p>
     * Handles various formats such as [URL name], comments, special prefixes, and removes unnecessary parts like links, comments, and extra symbols.
     * </p>
     *
     * @param text The input text containing the artist name and possibly extra information
     * @return The extracted artist name, or an empty string if not available
     */
    public static String extractArtistName(String text) {
        if (text == null || text.isEmpty()) return "";
        // コメント(<!-- -->)を削除
        int commentStart = text.indexOf("<!--");
        if (commentStart >= 0) {
            text = text.substring(0, commentStart);
        }
        // 非公開は空文字
        if (text.startsWith("非公開") || text.startsWith("Undisclosed") || text.startsWith("[''???'']")) return "";
        // みなとそふと で始まる場合は全て消す
        if (text.startsWith("みなとそふと　")) return text.substring("みなとそふと　".length()).trim();
        // [URL 名前] の形式
        if (text.startsWith("[")) {
            int close = text.indexOf("]");
            if (close > 0) {
                String inside = text.substring(1, close);
                // 半角・全角スペース両対応
                int space = inside.indexOf(' ');
                int zspace = inside.indexOf('　');
                int split = -1;
                if (space >= 0 && zspace >= 0)
                    split = Math.min(space, zspace);
                else
                    split = Math.max(space, zspace);
                if (split >= 0 && split < inside.length() - 1) {
                    String name = inside.substring(split + 1).replace('*', '＊');
                    // カッコがあれば前方のみ
                    int paren = name.indexOf('(');
                    if (paren > 0) name = name.substring(0, paren);
                    paren = name.indexOf('（');
                    if (paren > 0) name = name.substring(0, paren);
                    return name.trim();
                }
            }
        }
        // <br>や(や（や[で区切られている場合
        String[] splitters = {"<br>", "<br/>", "（", "(", "　", " (", "（", "["};
        String candidate = text;
        for (String splitter : splitters) {
            int idx = candidate.indexOf(splitter);
            if (idx > 0) {
                candidate = candidate.substring(0, idx);
            }
        }
        // [URL 名前]が途中にある場合
        if (candidate.startsWith("[")) {
            int close = candidate.indexOf("]");
            if (close > 0) {
                String inside = candidate.substring(1, close);
                // 半角・全角スペース両対応
                int space = inside.indexOf(' ');
                int zspace = inside.indexOf('　');
                int split = -1;
                if (space >= 0 && zspace >= 0)
                    split = Math.min(space, zspace);
                else
                    split = Math.max(space, zspace);
                if (split >= 0 && split < inside.length() - 1) {
                    candidate = inside.substring(split + 1);
                }
            }
        }
        // (や（で区切られている場合
        int paren = candidate.indexOf('（');
        if (paren > 0) candidate = candidate.substring(0, paren);
        paren = candidate.indexOf('(');
        if (paren > 0) candidate = candidate.substring(0, paren);
        // 末尾の空白除去
        candidate = candidate.trim();
        // yaman** → yaman＊＊
        candidate = candidate.replace("**", "＊＊");
        return candidate;
    }

}