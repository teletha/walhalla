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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import kiss.I;
import walhalla.open2ch.OpenThreadCollector;
import walhalla.open2ch.Res;
import walhalla.open2ch.Topic;

public class ArtTopicBuilder {

    public static void buildArtTopic(int year, int month) {
        ArtTopics topics = I.make(ArtTopics.class);
        Topic topic = new Topic();
        topic.category = "芸術";
        topic.title = year + "年" + month + "月の芸術鑑賞会";
        topic.description = "紳士淑女の皆さま。今宵の高尚なる芸術鑑賞の集いへとお運びいただき、まことに光栄に存じます。本席にて披露されますのは、才気あふれる絵師諸氏の筆より生まれた珠玉のアートの数々。――この場はあくまで教養の場、決して「うひょー！」等と叫ぶためのスレではございませんので悪しからず。";
        topic.published = LocalDateTime.now();

        AtomicInteger count = new AtomicInteger(1);
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

                if (res.date.getYear() != year || res.date.getMonthValue() != month) {
                    continue; // Skip if the comment is not from the specified month and year
                }

                if (!isArtisticPost(res.body)) {
                    continue; // Skip if the body does not contain artistic keywords
                }

                if (!hasPraiseReference(res.from.stream().map(n -> thread.getCommentBy(n).body).toList())) {
                    continue; // Skip if there are praise references in the replies
                }

                topic.comments.add((res.num + count.get() * 1000) * -1);
                for (int comment : res.from) {
                    topic.comments.add(comment + count.get() * 1000);
                }
                topic.extra.add(thread.num + "-" + thread.id);
                count.incrementAndGet();
            }
        });

        topics.add(topic);
        topics.store();
    }

    private static final List<String> keyword = List
            .of("ラク", "らく", "落", "イラ", "描", "書", "かいて", "かいた", "かきかき", "カキカキ", "かきこ", "カキコ", "出来た", "できた", "投下", "ぺた", "ペタ", "絵", "盛る", "応援", "投票");

    private static boolean isArtisticPost(String body) {
        for (String k : keyword) {
            if (body.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private static final List<String> praise = List
            .of("かわ", "きゃわ", "あぁ", "可愛", "素晴", "素敵", "最高", "上手", "好き", "うひょ", "むひょ", "えっっ", "エッッ", "ｴｯｯ", "でっっ", "デッッ", "ﾃﾞｯｯ", "でっか", "でか", "デカ", "えっち", "エッチ", "ｴﾁ", "えろ", "エロ", "エッロ", "えっろ", "えちち", "エチチ", "ｴﾁﾁ", "叡智", "ムチ", "ﾑﾁ", "むっちり", "ムッチリ", "スケベ", "おっぱい", "ちっぱい", "乳首", "胸", "けつ", "ケツ", "盛って", "盛り");

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

        return 2 <= count;
    }
}
