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

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import kiss.XML;
import walhalla.Astro;
import walhalla.util.Server;

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

    public void add(String id) {
        all++;
        if (!tweets.containsKey(id)) {
            requests.add(id);
            count++;
        }
        System.out.println(count + "/" + all);
    }

    public void fetch() {
        List<String> list = new ArrayList();
        for (String id : requests) {
            if (list.size() < 1) {
                list.add(id);
            }
        }

        if (!list.isEmpty()) {
            Server server = new Server();

            for (String uri : list) {
                uri = uri.replace("x.com", "nitter.net");
                XML root = I.xml(server.fetchSource(uri));
                XML main = root.find(".main-tweet");

                Author author = new Author();
                author.icon = main.find(".avatar").attr("src");
                author.name = main.find(".fullname").attr("title");
                author.userName = main.find(".username").attr("title");

                Tweet tweet = new Tweet();
                tweet.text = main.find(".tweet-content").text();
                main.find(".attachment").forEach(item -> {
                    tweet.media.add(item.attr("href"));
                });

                System.out.println(author);
                System.out.println(tweet);
            }

            server.shutdown();
        }

        // save tweets
        store();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path locate() {
        return Astro.ASSETS.file("tweets.json").asJavaPath();
    }
}
