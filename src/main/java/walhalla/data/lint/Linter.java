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

public class Linter {

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

    public String fix(String input) {
        return fix(input, null);
    }

    public String fix(String input, String description) {
        for (Lint lint : lints) {
            LintResult result = lint.fix(input);
            if (result.fixed()) {
                if (description != null) {
                    System.out.println(description);
                    System.out.println(input);
                    System.out.println(result.result());
                    System.out.println("");
                }
                input = result.result();
            }
        }
        return input;
    }
}
