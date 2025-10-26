/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.open2ch;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kiss.I;
import kiss.JSON;
import kiss.Storable;
import kiss.Variable;
import kiss.XML;
import psychopath.File;
import walhalla.Astro;
import walhalla.data.Nicknames;
import walhalla.image.Gyazo;
import walhalla.image.Image;
import walhalla.image.Imgur;
import walhalla.image.General;
import walhalla.tweet.Tweets;

/**
 * Represents a discussion thread on open2ch, including its metadata and all comments.
 * This class is responsible for parsing the cached thread HTML file and converting it
 * into structured data. It also handles storage and retrieval of the parsed content
 * as a JSON file for later use.
 */
public class OpenThread implements Storable<OpenThread> {

    private static final String EOL = System.lineSeparator();

    /** Formatter for parsing comment timestamps in the thread HTML. */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd(EEE) HH:mm:ss", Locale.JAPANESE);

    /** The thread's title (usually derived from the directory name). */
    public String title;

    /** The thread's number. */
    public int num;

    /** The thread's id. */
    public int id;

    /** The canonical URL of the thread. */
    public String url;

    /** The list of comments (res) contained in this thread. */
    public List<Res> comments = new ArrayList<>();

    /** The list of topics extracted from this thread's comments via LLM analysis. */
    private Topics topics;

    public OpenThread() {
    }

    public OpenThread(int num, int id) {
        this.num = num;
        this.id = id;

        restore();
    }

    public File parsedJSON() {
        return Astro.ARTICLE.directory(range(num)).directory(num + "-" + id).file("thread.json");
    }

    public File topicJSON() {
        return Astro.ARTICLE.directory(range(num)).directory(num + "-" + id).file("topic.json");
    }

    private static String range(int num) {
        int remaining = num % 100;
        int start = num - remaining;
        int end = start + 99;
        return start + "-" + end;
    }

    /**
     * Retrieves a comment by its number. If the number is negative,
     * it returns the comment counted from the end.
     *
     * @param num The comment number (starting from 1).
     * @return The corresponding {@link Res} comment.
     */
    public Res getCommentBy(int num) {
        return comments.get(Math.abs(num) - 1);
    }

    /**
     * Returns a list of topics extracted from the comments in this thread.
     *
     * @return A list of extracted {@link Topic} objects.
     */
    public List<Topic> getTopics() {
        if (topics == null) {
            if (topicJSON().isPresent()) {
                topics = I.json(topicJSON().text(), Topics.class);
            } else {
                analyze();
            }

            if (topics == null) topics = new Topics();
            topics.forEach(topic -> topic.thread = this);
        }
        return topics;
    }

    /**
     * Specifies where the JSON representation of this thread will be stored.
     *
     * @return The file path of the persisted thread data.
     */
    @Override
    public Path locate() {
        return parsedJSON().asJavaPath();
    }

    private int exaxtNumber(String text) {
        return Integer.parseInt(text.replaceAll("\\D", ""));
    }

    /**
     * Parses the HTML file (`thread.html`) in the given directory to extract thread metadata and
     * comments.
     * <p>
     * Each comment is parsed into a {@link Res} object including metadata such as:
     * <ul>
     * <li>Comment number</li>
     * <li>User name and ID</li>
     * <li>Timestamp</li>
     * <li>Body text</li>
     * <li>Image URLs (e.g. Imgur)</li>
     * <li>Embedded content (e.g. YouTube, X links)</li>
     * </ul>
     */
    void parse(String data) {
        XML html = I.xml("<!DOCTYPE html>" + data);

        this.title = html.element("title").text();
        this.num = exaxtNumber(this.title);
        this.url = html.find("head meta[property='og:url']").attr("content");
        this.id = exaxtNumber(this.url.substring(28));

        Nicknames nick = I.make(Nicknames.class);

        html.find("div.thread > dl").forEach(dl -> {
            int num = Integer.parseInt(dl.attr("val"));
            XML dt = dl.firstChild();
            XML name = dt.firstChild().next();
            String head = dl.firstChild().text();
            head = head.substring(head.indexOf('：', head.indexOf('：') + 1) + 1, head.indexOf("ID:")).trim();
            LocalDateTime date = LocalDateTime.parse(head, FORMATTER);
            String id = name.next().attr("val");

            XML dd = dt.next();

            // ======================================================
            // Collect images (usually Imgur links)
            // ======================================================
            List<ImageSource> images = new ArrayList<>();
            I.signal(dd.element("a")).take(e -> !e.attr("data-lightbox").isEmpty()).to(e -> {
                String href = e.attr("href");
                if (href.startsWith("//")) href = "https:" + href;
                ImageSource source = new ImageSource();
                source.origin = href;
                images.add(source);
                e.remove();
            });

            // ======================================================
            // Collect embedded content links (YouTube, X, etc.)
            // ======================================================
            List<String> embeds = new ArrayList<>();
            I.signal(dd.element("a")).take(e -> {
                String href = e.attr("href");
                if (href.startsWith("//")) href = "https:" + href;
                return href.startsWith("http") && !href.contains("open2ch");
            }).to(link -> {
                String href = link.attr("href");
                if (href.startsWith("https://imgur.com/")) {
                    if (href.endsWith(".jpeg") || href.endsWith(".jpg") || href.endsWith(".png") || href.endsWith(".gif") || href
                            .endsWith(".webp")) {
                        // Missed direct image link
                        ImageSource source = new ImageSource();
                        source.origin = href.replace("https://imgur.com/", "https://i.imgur.com/");
                        images.add(source);
                    } else {
                        ImageSource source = new ImageSource();
                        source.origin = Imgur.extractDirectImageURL(href);
                        images.add(source);
                    }
                } else {
                    embeds.add(href);
                }
                link.remove();
            });
            I.signal(dd.element("div")).take(div -> div.hasClass("nico")).to(div -> {
                String link = div.text();
                embeds.add(link.substring(6, link.length() - 1)); // Remove surrounding quotes
                div.remove();
            });

            // ======================================================
            // Extract and format the comment body
            // =====================================================
            dd.element("br").text("  \r\n");
            dd.element("b").forEach(b -> b.text("<b>" + b.text() + "</b>"));
            String body = dd.text().trim();

            body = body.replaceAll("&gt;&gt;(\\d+)\\s+", "<i>$1</i>");
            body = body.replaceAll("(?i)\\bhttps?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", "<a class=\"external\" href=\"$0\">$0</a>");
            body = body.replaceAll("(&amp;|\\?)\\w=[\\w\\-]+\\s*", "");
            body = nick.link(body);

            Res res = new Res();
            res.num = num;
            res.id = id;
            res.date = date;
            res.name = name.text().replace("↓", "");
            res.body = body;
            res.embeds = embeds;
            res.sources = images;

            this.comments.add(res);
        });

        linkageComments();

        store();
        I.info("Parse the thread " + num + " and cache it to [" + parsedJSON() + "].");
    }

    /**
     * Linkage references between comments.
     */
    private void linkageComments() {
        Pattern pattern = Pattern.compile("<i>(\\d+)</i>");

        for (Res res : comments) {
            res.from = new ArrayList();
            res.to = new ArrayList();

            Matcher matcher = pattern.matcher(res.body);
            while (matcher.find()) {
                String ref = matcher.group(1);
                Integer num = Integer.valueOf(ref);
                if (num < res.num) {
                    res.to.add(num);
                    comments.get(num - 1).from.add(res.num);
                }
            }
        }
    }

    /**
     * Performs topic extraction from all comments using an LLM (Gemini).
     * <p>
     * This method constructs a single prompt string by concatenating all comment bodies
     * and media links, then sends it to a language model to extract high-level discussion topics.
     */
    public void analyze(Instruction... instructions) {
        File topicJSON = topicJSON();

        if ((topicJSON.isAbsent() || topicJSON.size() == 0) && comments.size() >= 975) {
            I.info("Analyzing topics in thread " + num + " by Gemini.");
            topics = I.json(Editor.topics(composeThreadText()), Topics.class);
            topics.normalize(this);

            topicJSON.text(I.write(topics)).creationTime(0);
            I.info("Finish analyzing topics and cache it to [" + topicJSON + "].");
        }

        instructions = Stream.of(instructions).filter(i -> i.id() == id).toArray(Instruction[]::new);
        if (instructions.length != 0) {
            I.info("Analyzing additional topics " + I.signal(instructions)
                    .flatArray(x -> x.keywords())
                    .toList() + " in thread " + num + " by Gemini.");

            Topics addtional = I.json(Editor.topics(composeThreadText(), instructions), Topics.class);
            addtional.normalize(this);

            topicJSON.text(I.write(((Topics) getTopics()).merge(addtional))).creationTime(0);
            I.info("Finish analyzing addtional topics and cache it to [" + topicJSON + "].");
        }
    }

    private String composeThreadText() {
        StringBuilder text = new StringBuilder();
        for (Res res : comments) {
            text.append("#").append(res.num).append(EOL);
            text.append(res.date.format(FORMATTER)).append(EOL);
            text.append(res.body.replaceAll("<i>(\\d+)</i>", ">> $1\n")).append("\n");
            text.append(res.sources.stream().map(s -> s.origin).collect(Collectors.joining(EOL)));
            text.append(res.embeds.stream().collect(Collectors.joining(EOL)));
            text.append("\n\n");
        }
        return text.toString();
    }

    public void backupImages() {
        if (comments.get(comments.size() - 1).date.plusHours(3).isBefore(LocalDateTime.now())) {
            Set<OpenThread> modifieds = new HashSet();

            for (Topic topic : getTopics()) {
                for (int num : topic.comments) {
                    Res res = topic.getCommentBy(num);
                    for (ImageSource source : res.sources) {
                        if (!source.hasBackup()) {
                            Image image = null;

                            try {
                                if (source.origin.startsWith("https://i.imgur.com/")) {
                                    image = Imgur.download(source.origin);
                                } else {
                                    image = General.download(source.origin);
                                }
                            } catch (Throwable e) {
                                continue;
                            }

                            JSON origin = Gyazo.upload(image.originName(), image.origin());
                            String originURL = origin.text("url");
                            JSON originMeta = Gyazo.meta(originURL);
                            source.backup = List.of(originURL, originMeta.text("width"), originMeta.text("height"));

                            modifieds.add(res.thread);
                        }
                    }
                }
            }

            for (OpenThread thread : modifieds) {
                thread.store();
            }
        }
    }

    public void linkageCharacter() {
        Nicknames nick = I.make(Nicknames.class);

        for (Res res : comments) {
            res.body = nick.link(res.decodedBody());
        }
        store();
    }

    public Variable<String> searchNextURL() {
        Pattern pattern = Pattern.compile("https://uni.open2ch.net/test/read.cgi/gameswf/(\\d+)");
        for (int i = Math.min(comments.size() - 1, 1000); 100 < i; i--) {
            Res res = comments.get(i);
            Matcher matcher = pattern.matcher(res.body);
            if (matcher.find()) {
                return Variable.of(matcher.group(0));
            }
        }
        return Variable.empty();
    }

    /**
     * 
     */
    public void analyzeTweet() {
        Tweets tweets = I.make(Tweets.class);

        for (Topic topic : getTopics()) {
            if (topic.thread != null) {
                for (Integer id : topic.comments) {
                    Res res = topic.getCommentBy(id);

                    for (String url : res.embeds) {
                        if (url.startsWith("https://x.com/")) {
                            int index = url.lastIndexOf("/");
                            String tweetId = url.substring(index + 1);

                            try {
                                Long.parseLong(tweetId);
                                tweets.add(url, tweetId);
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                }
            }
        }
    }
}
