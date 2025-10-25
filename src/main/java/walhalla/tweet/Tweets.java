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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import walhalla.Astro;

@Managed(Singleton.class)
public class Tweets implements Storable<Tweets> {

    public Map<String, Tweet> tweets = new ConcurrentSkipListMap();

    public Map<String, Author> authors = new ConcurrentSkipListMap();

    private Set<String> requests = new HashSet();

    public Tweets() {
        restore();
    }

    static int all;

    static int count;

    public void add(String uri, String id) {
        all++;
        if (!tweets.containsKey(id)) {
            requests.add(uri);
            count++;
        }
        System.out.println(count + "/" + all);
    }

    public void fetch() {
        List<String> list = new ArrayList();
        for (String id : requests) {
            if (list.size() < 100) {
                list.add(id);
            }
        }

        for (String uri : list) {
            Nitter.fetchTweet(uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path locate() {
        return Astro.ASSETS.file("tweets.json").asJavaPath();
    }
}
