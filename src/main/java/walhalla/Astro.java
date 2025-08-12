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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import kiss.I;
import kiss.XML;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;
import walhalla.data.Database;
import walhalla.data.Rarity;
import walhalla.data.Unit;
import walhalla.data.UnitMeta;
import walhalla.data.UnitMetaInfo;
import walhalla.image.EditableImage;
import walhalla.open2ch.OpenThreadCollector;
import walhalla.open2ch.Res;
import walhalla.tweet.BlueSky;
import walhalla.tweet.Twitter;

/**
 * Provides utilities and entry point for the Astro project.
 * <p>
 * Handles directory management, unit database generation, unit icon sprite creation, and topic
 * analysis.
 * </p>
 */
public class Astro {

    public static final Set<String> NOT_IMPLEMENTED = Set.of("王国ファッションショー", "塔の守護人形ファリエ");

    /** Specify the english name only. */
    public static final Set<String> FORCE_UPDATE = Set.of();

    static {
        I.load(Astro.class);
    }

    /** The root directory of the Astro project. */
    private static final Directory ROOT = I.env("AstroRoot", Locator.directory(""));

    /** The article directory. */
    public static final Directory ARTICLE = ROOT.directory("article");

    /** The public directory. */
    public static final Directory PUBLIC = ROOT.directory("public");

    /** The public directory. */
    public static final Directory ASSETS = ROOT.directory("src/assets");

    /**
     * Internal database class for storing units by name.
     */
    @SuppressWarnings("serial")
    private static class FullDB extends HashMap<String, Unit> {
    }

    /**
     * Internal database class for storing units by name.
     */
    @SuppressWarnings("serial")
    private static class MetaDB extends HashMap<String, UnitMeta> {
    }

    /**
     * Builds the unit database by collecting units of specified rarities
     * and writes the result as a JSON file to the public directory.
     */
    public static void buildUnitJSON() {
        FullDB full = new FullDB();
        MetaDB meta = new MetaDB();
        Database manager = I.make(Database.class);
        manager.build();
        manager.by(Rarity.黒, Rarity.白, Rarity.金, Rarity.銀, Rarity.青).forEach(unit -> {
            full.put(unit.nameJ, unit);
            meta.put(unit.nameJ, unit.asMeta());
        });

        I.write(full, Astro.PUBLIC.file("characters.json").newBufferedWriter());
        I.write(meta, Astro.PUBLIC.file("meta.json").newBufferedWriter());
        I.write(new UnitMetaInfo(), Astro.PUBLIC.file("meta-info.json").newBufferedWriter());
    }

    /**
     * Builds a sprite image containing all unit icons, resizing each icon to 80px width,
     * and arranges them in a tiled format. Downloads missing icons if necessary.
     * The resulting sprite is saved as ".data/unit-icons.png".
     */
    public static void buildUnitIconSprite() {
        EditableImage container = new EditableImage();
        List<EditableImage> images = new ArrayList<>();

        Database db = I.make(Database.class);
        db.stream().sorted(Comparator.comparingInt(x -> x.id)).forEach(unit -> {
            File icon = Locator.file(".data/icon/" + unit.nameJ + ".png");
            if (icon.isAbsent()) {
                I.info("Downloaded icon for " + unit.nameJ + " to " + icon);
                I.http(unit.icon == null ? unit.iconAW : unit.icon, InputStream.class).waitForTerminate().to(input -> {
                    icon.writeFrom(input);
                });

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
            }

            EditableImage image = new EditableImage(icon.asJavaPath());
            images.add(image.resize(80));
        });

        File base = Locator.file(".data/unit-icons.png");
        container.tile(50, images).write(base.asJavaPath());

        String magick = I.env("IMAGE_MAGICK");
        File output = ASSETS.file("unit-icons40.avif");
        ProcessBuilder pb = new ProcessBuilder(magick, base.absolutize()
                .path(), "-filter", "Catrom", "-resize", "50%", "-define", "heic:encoder=avif", "-define", "heic:effort=3", "-quality", "40", output
                        .absolutize()
                        .path());
        try {
            I.info("Building unit icon sprite: " + pb.command());
            pb.inheritIO().start().waitFor();
        } catch (NullPointerException e) {
            throw new IllegalStateException("IMAGE_MAGICK environment variable is not set. Please set it to the path of ImageMagick executable.", e);
        } catch (InterruptedException | IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Collects and analyzes topics from open threads using OpenThreadCollector.
     * This method processes each thread and performs analysis.
     */
    public static void buildTopics() {
        OpenThreadCollector.findAll().to(thread -> {
            long start = System.currentTimeMillis();
            thread.analyze();
            thread.backupImages();
            thread.linkageCharacter();
            long end = System.currentTimeMillis();
            System.out.println("" + thread.title + " processed in " + (end - start) + " ms");
        });
    }

    public static void buildArtTopic() {
        OpenThreadCollector.findAll().to(thread -> {
            for (Res res : thread.comments) {
                if (res.num == 1) {
                    continue; // Skip the first comment
                }

                if (res.sources.isEmpty()) {
                    continue; // Skip if no images are attached
                }

                if (res.from.size() < 3) {
                    continue; // Skip if the comment has less than 3 replies
                }

                if (!isArtisticPost(res.body)) {
                    continue; // Skip if the body does not contain artistic keywords
                }

                if (!hasPraiseReference(res.from.stream().map(n -> thread.getCommentBy(n).body).toList())) {
                    continue; // Skip if there are praise references in the replies
                }
                System.out.println(thread.num + "  " + res.num + "   " + res.from + "   " + thread.url + "    " + res.body);
            }
        });
    }

    private static final List<String> keyword = List
            .of("ラク", "らく", "落", "イラ", "描", "書", "かいて", "かいた", "かきかき", "カキカキ", "かきこ", "カキコ", "出来た", "できた", "投下", "ぺた", "ペタ");

    private static boolean isArtisticPost(String body) {
        for (String k : keyword) {
            if (body.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private static final List<String> praise = List
            .of("かわいい", "かわゆい", "可愛", "素晴", "素敵", "最高", "上手", "好き", "うひょ", "むひょ", "えっっ", "エッッ", "ｴｯｯ", "でっっ", "デッッ", "ﾃﾞｯｯ", "でっか", "でか", "えっち", "エッチ", "えろ", "エロ", "エッロ", "えっろ", "えちち", "エチチ", "ｴﾁﾁ", "ムチ", "ﾑﾁ", "むっちり", "ムッチリ", "おっぱい", "盛って", "盛り");

    private static boolean hasPraiseReference(List<String> responses) {
        List<String> aaa = new ArrayList<>();
        int count = 0;
        for (String k : praise) {
            for (String body : responses) {
                if (body.contains(k)) {
                    count++;
                    aaa.add(k);
                }
            }
        }

        return 3 <= count;
    }

    public static void tweet() {
        File file = Locator.file(".data/tweet.log");
        String text = file.text();

        Twitter twitter = new Twitter();
        BlueSky blue = new BlueSky();

        I.http("https://wannyan.ephtra.workers.dev/rss.xml", XML.class)
                .flatIterable(xml -> xml.find("item"))
                .takeUntil(x -> x.element("link").text().equals(text))
                .reverse()
                .skip(1)
                .waitForTerminate()
                .to(item -> {
                    String title = item.element("title").text();
                    String link = item.element("link").text();
                    String description = item.element("description").text();
                    String image = item.element("media:content").attr("url");

                    twitter.tweet(title, description, link, image, "#千年戦争アイギス").waitForTerminate().to(json -> {
                        I.info("Tweet on Twitter: " + title);
                    });

                    blue.tweet(title, description, link, image, "#千年戦争アイギス").waitForTerminate().to(json -> {
                        I.info("Tweet on BlueSky: " + title);
                    });

                    file.text(link);
                });
    }

    /**
     * Main entry point. Builds the topics database.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // buildUnitJSON();
        // buildTopics();
        buildArtTopic();
    }
}