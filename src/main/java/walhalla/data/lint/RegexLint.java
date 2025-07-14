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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexLint implements Lint {

    private final Pattern pattern;

    private final String replacement;

    public RegexLint(String regex, String replacement) {
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LintResult fix(String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            return new LintResult(true, matcher.replaceAll(replacement));
        } else {
            return Lint.super.fix(input);
        }
    }
}
