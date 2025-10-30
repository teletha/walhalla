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

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EditedText {

    private static final String EOL = System.lineSeparator();

    /** Formatter for parsing comment timestamps in the thread HTML. */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd(EEE) HH:mm:ss", Locale.JAPANESE);

    private StringBuilder text = new StringBuilder();

    public int count;

    private Map<String, Integer> threads = new LinkedHashMap();

    private AtomicInteger counter;

    public EditedText() {
        this(0);
    }

    public EditedText(int startIndex) {
        counter = new AtomicInteger(startIndex);
    }

    public void add(Res res) {
        if (res.num <= 1 || 1000 <= res.num) {
            return; // out of range
        }

        count++;
        String id = res.thread.num + "-" + res.thread.id;
        int prefix = threads.computeIfAbsent(id, key -> counter.getAndIncrement());

        text.append("#").append(prefix * 1000 + res.num).append(EOL);
        text.append(res.date.format(FORMATTER)).append(EOL);
        text.append(res.body.replaceAll("<i>(\\d+)</i>", ">> $1\n")).append("\n");
        text.append(res.sources.stream().map(s -> s.origin).collect(Collectors.joining(EOL)));
        text.append(res.embeds.stream().collect(Collectors.joining(EOL)));
        text.append("\n\n");
    }

    public Collection<String> threads() {
        return threads.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return text.toString();
    }
}
