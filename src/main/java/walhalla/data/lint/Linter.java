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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Linter addRulePattern(String pattern, String replacement) {
        for (Expanded p : expandOptionals(pattern)) {
            String replaced = applyOptionRefs(replacement, p.options);
            fixer.addKeyword(p.text, replaced);
        }
        return this;
    }

    private String applyOptionRefs(String replacement, List<String> options) {
        Matcher matcher = Pattern.compile("\\$(\\d+)").matcher(replacement);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            String value = (index < options.size()) ? options.get(index) : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
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

    public static List<Expanded> expandOptionals(String input) {
        List<String> staticParts = new ArrayList<>();
        List<List<String>> optionGroups = new ArrayList<>();

        Matcher m = Pattern.compile("\\[(.+?)\\]").matcher(input);
        int lastEnd = 0;

        while (m.find()) {
            staticParts.add(input.substring(lastEnd, m.start()));
            String content = m.group(1);
            String[] options = content.split("\\|");
            List<String> optionsWithEmpty = new ArrayList<>();
            optionsWithEmpty.add(""); // "なし" パターン
            optionsWithEmpty.addAll(Arrays.asList(options));
            optionGroups.add(optionsWithEmpty);
            lastEnd = m.end();
        }
        staticParts.add(input.substring(lastEnd));

        int total = 1;
        for (List<String> group : optionGroups) {
            total *= group.size();
        }

        List<Expanded> results = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            int idx = i;
            StringBuilder sb = new StringBuilder();
            Expanded expanded = new Expanded();

            for (int j = 0; j < optionGroups.size(); j++) {
                sb.append(staticParts.get(j));
                List<String> group = optionGroups.get(j);
                int choice = idx % group.size();
                String selected = group.get(choice);
                sb.append(selected);
                expanded.options.add(selected);
                idx /= group.size();
            }
            sb.append(staticParts.get(optionGroups.size()));
            expanded.text = sb.toString();
            results.add(expanded);
        }

        return results;
    }

    public static class Expanded {
        public String text;

        public List<String> options = new ArrayList();

    }
}
