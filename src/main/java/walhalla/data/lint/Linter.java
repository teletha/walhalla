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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import kiss.WiseTriConsumer;

public class Linter {

    public static WiseTriConsumer<String, String, String> INTERCEPTOR;

    private final List<Lint> lints = new ArrayList<>();

    private final TrieLint fixer = new TrieLint();

    private final TrieLint normalizer = new TrieLint();

    public Linter() {
        add(normalizer);
        add(fixer);
    }

    public Linter normalize(String from, String to) {
        normalizer.addKeyword(from, to);
        return this;
    }

    public Linter add(Lint lint) {
        lints.add(lint);
        return this;
    }

    public Linter addRegex(String regex, String replacement) {
        return add(new RegexLint(regex, replacement));
    }

    public Linter addRule(String pattern, String replacement) {
        fixer.addKeyword(pattern, replacement);
        return this;
    }

    public Linter addTokenRule(String pattern, Consumer<LintResult> action) {
        fixer.addKeyword(pattern, (match, result) -> {
            action.accept(result);
            return "";
        });
        return this;
    }

    public LintResult fix(String input) {
        return fix(input, null);
    }

    public LintResult fix(String input, String description) {
        LintResult result = new LintResult(input);

        for (Lint lint : lints) {
            lint.fix(result);
        }

        if (description != null && INTERCEPTOR != null) {
            INTERCEPTOR.accept(description, result.original, result.text);
        }
        return result;
    }
}
