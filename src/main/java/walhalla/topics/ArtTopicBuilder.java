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

import java.util.ArrayList;
import java.util.List;

import walhalla.open2ch.OpenThreadCollector;
import walhalla.open2ch.Res;

public class ArtTopicBuilder {

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
}
