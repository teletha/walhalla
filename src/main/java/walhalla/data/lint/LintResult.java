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

public class LintResult {

    public final String original;

    public String text;

    public final List<String> variables = new ArrayList();

    public LintResult(String original) {
        this.original = original;
        this.text = original;
    }
}
