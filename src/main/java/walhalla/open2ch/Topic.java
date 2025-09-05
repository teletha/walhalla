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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a semantically grouped topic extracted from a thread's comments.
 * Each topic contains a title, associated comment numbers, metadata such as category and tags,
 * and logic to render/publish it as a Markdown article.
 */
public class Topic {

    /** The title or summary of this topic, typically generated via LLM. */
    public String title;

    /** The list of comment numbers (1-based) that are relevant to this topic. */
    public List<Integer> comments = new ArrayList();

    /** Optional category label for the topic (used in publishing). */
    public String category;

    /** Optional tag labels for the topic (used in publishing). */
    public List<String> tags = new ArrayList();

    public String description;

    public int priority;

    public List<String> extra = new ArrayList();

    public LocalDateTime published;

    /** The thread from which this topic was extracted. */
    OpenThread thread;

    /** Cache for additional threads that may be related to this topic. */
    List<OpenThread> extraThreads;

    /**
     * Retrieves a comment by its number. If the number is negative,
     * it returns the comment counted from the end.
     *
     * @param num The comment number (starting from 1).
     * @return The corresponding {@link Res} comment.
     */
    public synchronized Res getCommentBy(int num) {
        num = Math.abs(num);

        if (num <= 1000 || extra.isEmpty()) {
            Res res = thread.getCommentBy(num);
            res.thread = thread;
            return res;
        } else {
            if (extraThreads == null) {
                extraThreads = new ArrayList();
                for (String threadId : extra) {
                    extraThreads.add(OpenThreadCollector.findBy(threadId));
                }
            }

            int id = num % 1000;
            int index = ((num - id) / 1000) - 1;

            OpenThread thread = extraThreads.get(index);
            Res res = thread.getCommentBy(id);
            res.thread = thread;
            return res;
        }
    }
}
