/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.topics;

import java.nio.file.Path;

import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import walhalla.Astro;
import walhalla.open2ch.Topics;

@SuppressWarnings("serial")
@Managed(Singleton.class)
public class ArtTopics extends Topics implements Storable<ArtTopics> {

    protected ArtTopics() {
        restore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path locate() {
        return Astro.ARTICLE.file("15100-15199/15171-1748591399/topic.json").asJavaPath();
    }
}
