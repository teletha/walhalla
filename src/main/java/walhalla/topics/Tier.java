/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.topics;

import java.util.HashSet;
import java.util.Set;

public class Tier implements Comparable<Tier> {

    public String name;

    public int trend;

    public Set<String> trendWords = new HashSet<>();

    public int tower;

    public int majin;

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Tier o) {
        return score() - o.score();
    }

    public void addWord(String word) {
        trend++;
        trendWords.add(word);
    }

    public int score() {
        return trend + tower + majin;
    }
}
