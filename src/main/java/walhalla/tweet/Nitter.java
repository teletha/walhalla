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

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import kiss.I;
import kiss.XML;

public class Nitter {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("MMM d, yyyy '·' h:mm a 'UTC'")
            .toFormatter(Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    public static Tweet fetchTweet(String uri) {
        XML root = fetchXML(uri);
        if (root == null) return null;
        XML main = root.find(".main-tweet");
        if (main.size() == 0) return null;

        Author author = new Author();
        author.icon = "https://nitter.net" + main.find(".avatar").attr("src");
        author.name = main.find(".fullname").attr("title");
        author.userName = main.find(".username").attr("title").substring(1);

        Tweet tweet = new Tweet();
        tweet.id = uri.substring(uri.lastIndexOf("/") + 1);
        tweet.author = author.userName;
        tweet.date = ZonedDateTime.parse(main.find(".tweet-date a").attr("title"), FORMATTER)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        tweet.text = main.find(".tweet-content").text();
        main.find(".attachment img").forEach(item -> {
            tweet.media.add("https://nitter.net" + item.attr("src"));
        });

        Tweets tweets = I.make(Tweets.class);
        tweets.authors.put(author.userName, author);
        tweets.tweets.put(tweet.id, tweet);
        tweets.store();

        return tweet;
    }

    public static XML fetchXML(String uri) {
        uri = uri.replace("x.com", "nitter.net");

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br, zstd")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Priority", "u=0, i")
                    .header("TE", "trailers");

            Thread.sleep(1000 * 10);

            System.out.println("Fetch tweet " + uri);
            return I.http(builder, XML.class).waitForTerminate().skipError().to().get();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
