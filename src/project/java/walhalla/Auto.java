/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla;

import bee.Task;
import bee.api.Command;
import kiss.I;

public interface Auto extends Task {

    @Command("Tweet the latest ariticles automatically.")
    default void tweet() {
        Astro.tweet();
    }

    default void commit() {
        System.out.println("Hook commit is not implemented yet.");
        I.info("Hook commit is not implemented yet.");
    }
}
