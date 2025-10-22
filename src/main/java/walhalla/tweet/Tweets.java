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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import kiss.I;
import kiss.JSON;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;

@Managed(Singleton.class)
public class Tweets implements Storable<Tweets> {

    public Map<String, Tweet> tweets = new ConcurrentSkipListMap();

    public Map<String, Author> authors = new ConcurrentSkipListMap();

    private Set<String> requests = new HashSet();

    public Tweets() {
        restore();
    }

    public void add(String id) {
        if (!tweets.containsKey(id)) {
            requests.add(id);
        }
    }

    public void fetch() {
        List<String> list = new ArrayList();
        for (String id : requests) {
            if (list.size() < 100) {
                list.add(id);
            }
        }

        // JSON root = new Twitter().fetch(list);
        JSON root = I.json(Path.of("tweet.json"));
        analyzeAuthor(root);
        analyzeTweet(root);

        // save tweets
        store();

    }

    private void analyzeAuthor(JSON json) {
        for (JSON item : json.find("includes", "users", "*")) {
            Author author = new Author();
            author.name = item.text("name");
            author.id = item.text("id");
            author.userName = item.text("username");
            author.icon = item.text("profile_image_url");

            authors.put(author.id, author);
        }
    }

    private void analyzeTweet(JSON json) {
        Map<String, String> medias = new HashMap();
        for (JSON media : json.find("includes", "media", "*")) {
            medias.put(media.text("media_key"), media.text("url"));
        }

        for (JSON item : json.find("data", "*")) {
            Tweet tweet = new Tweet();
            tweet.id = item.text("id");
            tweet.date = ZonedDateTime.parse(item.text("created_at"));
            tweet.author = item.text("author_id");
            tweet.text = item.text("text");

            for (String key : item.find(String.class, "attachments", "media_keys", "*")) {
                tweet.media.add(medias.get(key));
            }

            tweets.put(tweet.id, tweet);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path locate() {
        return Path.of(".data/tweets.json");
    }

    public static void main(String[] args) {
        I.make(Tweets.class).fetch();
    }
}
