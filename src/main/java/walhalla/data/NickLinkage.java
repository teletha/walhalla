/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ahocorasick.trie.PayloadToken;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;

@Managed(Singleton.class)
public class NickLinkage {

    private static final Map<String, List<String>> NICKS = new HashMap();

    static {
        NICKS.put("エレオノーラ", List.of("Yさん"));
        NICKS.put("セリア", List.of("ズッ友"));
        NICKS.put("ティール", List.of("ムーミン"));
        NICKS.put("キュウビ", List.of("毛玉"));
        NICKS.put("諸葛亮 孔明", List.of("はわわ"));
        NICKS.put("コーネリア", List.of("こねこね"));
        NICKS.put("ミネット", List.of("にゃんにゃか"));
        NICKS.put("ラクシャーサ ", List.of("麻宮"));
        NICKS.put("鬼刃姫", List.of("あてちゃん", "あて"));
        NICKS.put("ロアナ", List.of("阿部さん"));
        NICKS.put("オリヴィエ", List.of("織部"));
        NICKS.put("ケイティ", List.of("KT"));
        NICKS.put("アナトリア", List.of("チャンプ"));
        NICKS.put("リヴン", List.of("村娘"));
        NICKS.put("アスバール", List.of("お嬢", "総帥"));
        NICKS.put("アンナ", List.of("ンナァ"));
        NICKS.put("聞忠", List.of("んちゅ", "聞仲"));
        NICKS.put("ズィズィー", List.of("ZZ", "ズィズィ", "ズィ"));
        NICKS.put("ラシュマシュ", List.of("ラシュ", "マシュ"));
        NICKS.put("デルフィーナ", List.of("ピザ"));
        NICKS.put("メフィスト", List.of("メッフィー", "めっふぃー"));
        NICKS.put("アブグルント", List.of("アブちゃん"));
        NICKS.put("神野悪五郎", List.of("悪五郎"));
        NICKS.put("オスカー", List.of("手塚"));
        NICKS.put("清源妙道真君", List.of("妙ちゃん", "清源", "清原"));
        NICKS.put("天墜神星", List.of("アマツちゃん", "アマツ", "天墜ちゃん"));
        NICKS.put("祓剣主神", List.of("フツヌシ"));
        NICKS.put("山ン本五郎左衛門", List.of("山ン本"));
        NICKS.put("ヤシマ", List.of("狸", "たぬき", "黒狸"));
        NICKS.put("スズネ", List.of("白狸"));
        NICKS.put("ツァーユ", List.of("ツァ"));
        NICKS.put("徐華", List.of("女媧"));
        NICKS.put("ラタトスク", List.of("ラタ様", "ラタちゃん"));
        NICKS.put("ウルカノ", List.of("カノ"));
        NICKS.put("白の皇帝", List.of("皇帝"));
        NICKS.put("ヴィラヘルム", List.of("初代皇帝"));
        NICKS.put("アリシア", List.of("アリス"));
        NICKS.put("伏綺", List.of("伏犠"));
        NICKS.put("金糸雀姉妹", List.of("金糸雀"));
        NICKS.put("紫苑姉妹", List.of("紫苑"));
        NICKS.put("群青姉妹", List.of("みけいたち", "群青"));
        NICKS.put("金光聖菩", List.of("金ちゃん"));
        NICKS.put("太上老君", List.of("老君", "老くん", "ろーくん"));
        NICKS.put("ホルミース", List.of("詩人"));

        // 特殊略称
        // 名前の語尾をだけを取って季節接頭辞と合わせるために使用
        NICKS.put("オーガスタ", List.of("カレー", "ガスタ"));
        NICKS.put("リズリー", List.of("ズリー"));
        NICKS.put("クロノシア", List.of("ノシア", "シア"));
    }

    private final PayloadTrie<String> trie;

    private NickLinkage() {
        Database db = I.make(Database.class);

        PayloadTrieBuilder builder = PayloadTrie.builder().ignoreOverlaps();
        for (String name : db.uniqueSubNames()) {
            List<Unit> units = db.searchByName(name);

            register(builder, name, units);
            List<String> nicks = NICKS.get(name);
            if (nicks != null) {
                for (String nick : nicks) {
                    register(builder, nick, units);
                }
            }
        }
        builder.addKeyword("もりたん", "https://kuromojiya.sakura.ne.jp/aigis.htm");

        // 一般名詞の一部にキャラ名が含まれている場合にリンクを無効にするために
        // 名詞自体を登録しておく防衛戦略
        builder.addKeyword("バッファー", "");
        builder.addKeyword("レンジ", "");
        builder.addKeyword("ジレンマ", "");
        builder.addKeyword("ゴブリン", "");
        builder.addKeyword("リンゴ", "");
        builder.addKeyword("セーラー", "");

        this.trie = builder.build();
    }

    private static void register(PayloadTrieBuilder builder, String name, List<Unit> units) {
        units.stream().filter(u -> u.nameJ.startsWith("ちび")).forEach(u -> {
            builder.addKeyword("ちび" + name, "/character/" + u.nameJ + "/");
        });

        units.stream().filter(u -> u.isSeasonal()).forEach(u -> {
            for (Attribute season : u.season()) {
                for (String prefix : season.seasonPrefix) {
                    builder.addKeyword(prefix + name, "/character/" + u.nameJ + "/");
                }
            }
        });

        units.stream().filter(u -> u.rarity == Rarity.白).findFirst().ifPresent(u -> {
            builder.addKeyword("白" + name, "/character/" + u.nameJ + "/");
        });
        units.stream().filter(u -> u.rarity == Rarity.黒).findFirst().ifPresent(u -> {
            builder.addKeyword("黒" + name, "/character/" + u.nameJ + "/");
            builder.addKeyword("新装" + name, "/character/" + u.nameJ + "/");
        });
        units.stream().findFirst().ifPresent(u -> {
            builder.addKeyword("素" + name, "/character/" + u.nameJ + "/");
            builder.addKeyword("通常" + name, "/character/" + u.nameJ + "/");
            builder.addKeyword("ノーマル" + name, "/character/" + u.nameJ + "/");
        });

        if (units.size() == 1) {
            builder.addKeyword(name, "/character/" + units.getFirst().nameJ + "/");
        } else {
            builder.addKeyword(name, "/type/" + units.getFirst().subNameJ + "/");
        }
    }

    public String link(String input) {
        StringBuilder result = new StringBuilder();
        Set<String> recoder = new HashSet();
        Collection<PayloadToken<String>> tokens = trie.tokenize(input);

        for (PayloadToken<String> token : tokens) {
            if (token.isMatch() && recoder.add(token.getFragment())) {
                String payload = token.getEmit().getPayload();
                if (payload.isEmpty()) {
                    result.append(token.getFragment());
                } else {
                    result.append("<a href='").append(payload).append("'>").append(token.getFragment()).append("</a>");
                }
            } else {
                result.append(token.getFragment());
            }
        }
        return result.toString();
    }
}
