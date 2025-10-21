/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.open2ch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A specialized list of {@link Topic} objects representing extracted discussion themes.
 * This class is used for serialization and deserialization of topic analysis results.
 */
@SuppressWarnings("serial")
public class Topics extends ArrayList<Topic> {

    /**
     * Normalizes the list of topics by applying the following rules:
     * <ul>
     * <li>Removes topics whose comment count is less than 10 or more than 40.</li>
     * <li>Converts all half-width symbols in topic titles to full-width characters.</li>
     * </ul>
     * <p>
     * This method is intended to enforce data constraints and ensure consistent formatting
     * for topic titles, especially for use in UI or documentation where full-width symbols are
     * preferred.
     */
    void normalize(OpenThread thread) {
        removeIf(topic -> {
            int size = topic.comments.size();
            return size < 13 || 55 < size;
        });

        forEach(topic -> {
            topic.title = convertHalfToFullSymbols(topic.title);
            topic.comments = complementReference(thread, topic.comments);
            topic.comments = sortReference(thread, topic.comments);
            topic.comments = normalizeComment(topic.comments);
        });
    }

    private List<Integer> normalizeComment(List<Integer> comments) {
        comments.removeIf(x -> x == 1 || 1000 <= x);
        return comments;
    }

    private List<Integer> complementReference(OpenThread thread, List<Integer> comments) {
        Set<Integer> used = new HashSet(comments.stream().map(x -> Math.abs(x)).toList());
        List<Integer> complements = new ArrayList();
        for (Integer num : comments) {
            Res comment = thread.getCommentBy(num);
            for (Integer referer : comment.to) {
                if (used.add(referer)) {
                    complements.add(referer);
                }
            }
            complements.add(num);
        }
        return complements;
    }

    /**
     * @param comments
     * @return
     */
    private List<Integer> sortReference(OpenThread thread, List<Integer> comments) {
        List<Integer> sorted = new ArrayList();
        while (!comments.isEmpty()) {
            Integer item = comments.remove(0);
            sorted.add(item);

            sort(sorted, comments, thread, item);
        }
        return sorted;
    }

    private void sort(List<Integer> sorted, List<Integer> origin, OpenThread thread, int num) {
        Res comment = thread.getCommentBy(num);
        for (Integer referer : comment.from) {
            if (origin.remove(referer)) {
                sorted.add(referer);
                sort(sorted, origin, thread, referer);
            }
        }
    }

    /**
     * Converts half-width ASCII symbols in the input string to their full-width equivalents.
     * <p>
     * This method targets only non-alphanumeric ASCII characters in the range U+0021 to U+007E.
     * Alphanumeric characters (letters and digits) and characters outside this range are left
     * unchanged.
     * <p>
     * Example:
     * <pre>
     * Input: "Hello! Are you #1?"
     * Output: "Hello！ Are you ＃1？"
     * </pre>
     *
     * @param input the input string possibly containing half-width ASCII symbols
     * @return a new string with half-width symbols converted to full-width
     */
    private static String convertHalfToFullSymbols(String input) {
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (ch >= 0x21 && ch <= 0x7E && !Character.isLetterOrDigit(ch)) {
                sb.append((char) (ch + 0xFEE0));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Merges the current list of topics with an additional list,
     * 
     * @param addtional
     * @return
     */
    Topics merge(Topics addtional) {
        Topics merged = new Topics();
        merged.addAll(this);
        merged.addAll(addtional);
        return merged;
    }
}