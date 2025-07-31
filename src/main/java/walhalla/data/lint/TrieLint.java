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

import org.ahocorasick.trie.PayloadToken;
import org.ahocorasick.trie.PayloadTrie;

public class TrieLint implements Lint {

    private final PayloadTrie.PayloadTrieBuilder<String> builder = PayloadTrie.<String> builder().ignoreOverlaps();

    private PayloadTrie<String> cachedTrie;

    public TrieLint addKeyword(String key, String value) {
        builder.addKeyword(key, value);
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
        Collection<PayloadToken<String>> tokens = cachedTrie.tokenize(lint.text);
        boolean fixed = false;
        for (PayloadToken<String> token : tokens) {
            if (token.isMatch()) {
                result.append(token.getEmit().getPayload());
                fixed = true;
            } else {
                result.append(token.getFragment());
            }
        }

        lint.text = result.toString();
    }
}