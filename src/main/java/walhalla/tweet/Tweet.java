/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.tweet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import kiss.I;

public class Tweet {

    public String id;

    public String author;

    public String text;

    public LocalDateTime date;

    public List<String> media = new ArrayList();

    public Author author() {
        return I.make(Tweets.class).authors.get(author);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Tweet [id=" + id + ", author=" + author + ", text=" + text + ", date=" + date + ", media=" + media + "]";
    }
}
