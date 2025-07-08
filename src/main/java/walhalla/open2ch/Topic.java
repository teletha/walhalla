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

    public List<String> units = new ArrayList();

    public int priority;
}
