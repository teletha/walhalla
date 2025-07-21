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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import kiss.I;
import kiss.JSON;
import kiss.Storable;
import kiss.XML;
import psychopath.Directory;
import psychopath.File;
import walhalla.Astro;
import walhalla.data.NickLinkage;
import walhalla.image.Gyazo;
import walhalla.image.Image;
import walhalla.image.Imgur;

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

    final File parsedJSON;

    private final File topicJSON;

    /**
     * Private constructor to prevent direct instantiation.
     */
    OpenThread(Directory root) {
        String name = root.name();
        int index = name.indexOf("-");
        this.num = Integer.parseInt(name.substring(0, index));
        this.id = Integer.parseInt(name.substring(index + 3));
        this.parsedJSON = root.file("thread.json");
        this.topicJSON = root.file("topic.json");

        restore();
    }

    OpenThread(String num, int id) {
        this(Astro.ARTICLE.directory(num + "-" + id));
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
            if (topicJSON.isPresent()) {
                topics = I.json(topicJSON.text(), Topics.class);
            } else {
                analyze();
            }
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
        return parsedJSON.asJavaPath();
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
        this.url = html.find("head meta[property='og:url']").attr("content");

        NickLinkage nick = I.make(NickLinkage.class);

        html.find("div.thread > dl").forEach(dl -> {
            int num = Integer.parseInt(dl.attr("val"));
            XML dt = dl.firstChild();
            XML name = dt.firstChild().next();
            String head = dl.firstChild().text();
            head = head.substring(head.indexOf('：', head.indexOf('：') + 1) + 1, head.indexOf("ID:")).trim();
            LocalDateTime date = LocalDateTime.parse(head, FORMATTER);
            String id = name.next().attr("val");

            XML dd = dt.next();
            System.out.println(dd);
            // ======================================================
            // Collect embedded content links (YouTube, X, etc.)
            // ======================================================
            List<String> embeds = new ArrayList<>();
            I.signal(dd.element("a")).take(e -> {
                String href = e.attr("href");
                if (href.startsWith("//")) href = "https:" + href;
                return href.startsWith("https://youtu.be/") || href.startsWith("https://x.com/");
            }).to(link -> {
                embeds.add(link.attr("href"));
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
            body = body.replaceAll("(?i)\\bhttps?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", "<a href=\"$0\">$0</a>");
            body = body.replaceAll("&amp;t=[^&]+\\s*", "");
            body = nick.link(body);

            List<ImageSource> images = new ArrayList<>();
            I.signal(dd.element("a")).take(e -> !e.attr("data-lightbox").isEmpty()).to(e -> {
                String href = e.attr("href");
                if (href.startsWith("//")) href = "https:" + href;
                ImageSource source = new ImageSource();
                source.origin = href;
                images.add(source);
            });

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

        store();
        I.info("Parse the thread " + num + " and cache it to [" + parsedJSON + "].");
    }

    /**
     * Performs topic extraction from all comments using an LLM (Gemini).
     * <p>
     * This method constructs a single prompt string by concatenating all comment bodies
     * and media links, then sends it to a language model to extract high-level discussion topics.
     */
    public void analyze() {
        if (topicJSON.isAbsent() || topicJSON.size() == 0) {
            I.info("Analyzing topics in thread " + num + " by Gemini.");
            topics = I.json(Editor.topics(composeThreadText()), Topics.class);
            topics.normalize();

            topicJSON.text(I.write(topics)).creationTime(0);
            I.info("Finish analyzing topics and cache it to [" + topicJSON + "].");
        }
    }

    private String composeThreadText() {
        StringBuilder text = new StringBuilder();
        for (Res res : comments) {
            text.append("#").append(res.num).append(EOL);
            text.append(res.date.format(FORMATTER)).append(EOL);
            text.append(res.body).append("\n");
            text.append(res.sources.stream().map(s -> s.origin).collect(Collectors.joining(EOL)));
            text.append(res.embeds.stream().collect(Collectors.joining(EOL)));
            text.append("\n\n");
        }
        return text.toString();
    }

    public void backupImages() {
        if (comments.getLast().date.plusDays(2).isBefore(LocalDateTime.now())) {
            boolean needUpdate = false;

            for (Topic topic : getTopics()) {
                for (int num : topic.comments) {
                    Res res = getCommentBy(num);
                    for (ImageSource source : res.sources) {
                        if (!source.hasBackup() && source.origin.startsWith("https://i.imgur.com/")) {
                            Image image = Imgur.download(source.origin);

                            JSON huge = Gyazo.upload(image.hugeName(), image.huge());
                            source.backupH = huge.text("url");

                            JSON large = Gyazo.upload(image.largeName(), image.large());
                            source.backupL = large.text("url");

                            needUpdate = true;
                        }
                    }
                }
            }

            if (needUpdate) {
                store();
            }

        }
    }

    public void linkageCharacter() {
        NickLinkage nick = I.make(NickLinkage.class);

        for (Res res : comments) {
            res.body = nick.link(unlink(res.body));
        }
        store();
    }

    private String unlink(String text) {
        return text.replaceAll("<a\\b[^>]*>(.*?)</a>", "$1");
    }

    /**
     * A specialized list of {@link Topic} objects representing extracted discussion themes.
     * This class is used for serialization and deserialization of topic analysis results.
     */
    @SuppressWarnings("serial")
    private static class Topics extends ArrayList<Topic> {

        /**
         * Normalizes the list of topics by applying the following rules:
         * <ul>
         * <li>Removes topics whose comment count is less than 10 or more than 40.</li>
         * <li>Converts all half-width symbols in topic titles to full-width characters.</li>
         * </ul>
         * <p>
         * This method is intended to enforce data constraints and ensure consistent formatting
         * for topic titles, especially for use in UI or documentation where full-width symbols are
         * preferred.
         */
        private void normalize() {
            removeIf(topic -> {
                int size = topic.comments.size();
                return size < 15 || 50 < size;
            });

            forEach(topic -> {
                topic.title = convertHalfToFullSymbols(topic.title);
            });
        }

        /**
         * Converts half-width ASCII symbols in the input string to their full-width equivalents.
         * <p>
         * This method targets only non-alphanumeric ASCII characters in the range U+0021 to U+007E.
         * Alphanumeric characters (letters and digits) and characters outside this range are left
         * unchanged.
         * <p>
         * Example:
         * <pre>
         * Input: "Hello! Are you #1?"
         * Output: "Hello！ Are you ＃1？"
         * </pre>
         *
         * @param input the input string possibly containing half-width ASCII symbols
         * @return a new string with half-width symbols converted to full-width
         */
        private static String convertHalfToFullSymbols(String input) {
            StringBuilder sb = new StringBuilder();
            for (char ch : input.toCharArray()) {
                if (ch >= 0x21 && ch <= 0x7E && !Character.isLetterOrDigit(ch)) {
                    sb.append((char) (ch + 0xFEE0));
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }
}
