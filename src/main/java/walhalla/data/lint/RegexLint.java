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
        boolean fixed = false;
        StringBuffer result = new StringBuffer();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            fixed = true;
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return new LintResult(fixed, result.toString());
    }
}
