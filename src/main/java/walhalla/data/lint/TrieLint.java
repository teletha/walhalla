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

import java.util.Collection;
import java.util.function.BiFunction;

import org.ahocorasick.trie.PayloadToken;
import org.ahocorasick.trie.PayloadTrie;

public class TrieLint implements Lint {

    private final PayloadTrie.PayloadTrieBuilder<BiFunction<String, LintResult, String>> builder = PayloadTrie
            .<BiFunction<String, LintResult, String>> builder()
            .ignoreOverlaps();

    private PayloadTrie<BiFunction<String, LintResult, String>> cachedTrie;

    public TrieLint addKeyword(String key, String value) {
        builder.addKeyword(key, (match, result) -> value);
        cachedTrie = null;
        return this;
    }

    public TrieLint addKeyword(String key, BiFunction<String, LintResult, String> action) {
        builder.addKeyword(key, action);
        cachedTrie = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fix(LintResult lint) {
        if (cachedTrie == null) {
            cachedTrie = builder.build();
        }

        StringBuilder result = new StringBuilder();
        Collection<PayloadToken<BiFunction<String, LintResult, String>>> tokens = cachedTrie.tokenize(lint.text);
        for (PayloadToken<BiFunction<String, LintResult, String>> token : tokens) {
            if (token.isMatch()) {
                result.append(token.getEmit().getPayload().apply(token.getFragment(), lint));
            } else {
                result.append(token.getFragment());
            }
        }

        lint.text = result.toString();
    }
}