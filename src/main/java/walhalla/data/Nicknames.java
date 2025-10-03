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
import java.util.function.Consumer;

import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadToken;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;

@Managed(Singleton.class)
public class Nicknames {

    private static final Map<String, List<String>> NICKS = new HashMap();

    private static final Map<String, List<String>> FULL_NICKS = new HashMap();

    static {
        NICKS.put("エレオノーラ", List.of("Yさん"));
        NICKS.put("セリア", List.of("ズッ友"));
        NICKS.put("ティール", List.of("ムーミン"));
        NICKS.put("キュウビ", List.of("毛玉"));
        NICKS.put("諸葛亮 孔明", List.of("はわわ"));
        NICKS.put("コーネリア", List.of("こねこね", "コネコネ"));
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
        NICKS.put("聞忠", List.of("んちゅ", "ぶんちゅー", "もんちゅー", "聞仲"));
        NICKS.put("誅子", List.of("ちゅーこ", "ちゅーし"));
        NICKS.put("ズィズィー", List.of("ZZ", "ＺZ", "ｚｚ", "ズィズィ", "ズィ", "ズイズイ", "ずいずい"));
        NICKS.put("ラシュマシュ", List.of("ラシュ", "マシュ"));
        NICKS.put("デルフィーナ", List.of("ピザ"));
        NICKS.put("メフィスト", List.of("メッフィー", "めっふぃー"));
        NICKS.put("テュト", List.of("おばあちゃん", "テュトおばあちゃん"));
        NICKS.put("アブグルント", List.of("アブちゃん"));
        NICKS.put("神野悪五郎", List.of("悪五郎", "アクゴロー", "あくごろー"));
        NICKS.put("オスカー", List.of("手塚"));
        NICKS.put("清源妙道真君", List.of("妙ちゃん", "清源", "清原"));
        NICKS.put("天墜神星", List.of("アマツちゃん", "アマツ", "天墜ちゃん"));
        NICKS.put("祓剣主神", List.of("フツヌシ", "ふつぬし"));
        NICKS.put("元始天尊", List.of("天尊"));
        NICKS.put("山ン本五郎左衛門", List.of("山ン本", "山本", "五郎左衛門", "ザエモン", "ざえもん"));
        NICKS.put("ヤシマ", List.of("狸", "たぬき", "黒狸"));
        NICKS.put("スズネ", List.of("白狸"));
        NICKS.put("ツァーユ", List.of("ツァ"));
        NICKS.put("ラタトスク", List.of("ラタ様", "ラタちゃん"));
        NICKS.put("ウルカノ", List.of("カノ"));
        NICKS.put("白の皇帝", List.of("皇帝"));
        NICKS.put("ヴィラヘルム", List.of("初代皇帝"));
        NICKS.put("アリシア", List.of("アリス"));
        NICKS.put("オラティル", List.of("お粥"));
        NICKS.put("伏綺", List.of("伏犠", "ふっき", "フッキ"));
        NICKS.put("金糸雀姉妹", List.of("金糸雀"));
        NICKS.put("紫苑姉妹", List.of("紫苑"));
        NICKS.put("群青姉妹", List.of("みけいたち", "群青"));
        NICKS.put("金光聖菩", List.of("金ちゃん"));
        NICKS.put("太上老君", List.of("老君", "老くん", "ろーくん", "ロークン"));
        NICKS.put("ホルミース", List.of("詩人"));
        NICKS.put("クゥイル", List.of("QIL"));
        NICKS.put("ハリンヘイム", List.of("ハリン"));
        NICKS.put("秋山凜子", List.of("凛子", "リンコ"));
        NICKS.put("スーシェン", List.of("スーチェン"));
        NICKS.put("ツグミ", List.of("鵺"));
        NICKS.put("ヨユキ", List.of("夜行さん", "夜行さん現当主ヨユキ"));
        NICKS.put("ロイジィ", List.of("ロイ爺", "ロイジー", "ろいじー", "ろいじい", "ロイジイ"));
        NICKS.put("アイギス神殿", List.of("神殿ちゃん", "ギス神殿"));
        NICKS.put("マーガレット", List.of("マガレ"));
        NICKS.put("徐華", List.of("ジョカ", "女媧", "徐媧"));
        NICKS.put("曹操", List.of("華林", "かりん", "カリンちゃん", "カリン様"));
        NICKS.put("ムルーア", List.of("赤魚"));
        NICKS.put("ピュレスカ", List.of("青魚"));
        NICKS.put("エスネア", List.of("ぽんちゃん", "ポンちゃん"));
        NICKS.put("ラクサーシャ", List.of("麻宮"));

        FULL_NICKS.put("旗艦乙女ドーンブリンガー", List.of("どんぶり", "ドンブリ", "あかつき", "アカツキ", "暁"));

        // 特殊略称
        // 名前の語尾をだけを取って季節接頭辞と合わせるために使用
        NICKS.put("オーガスタ", List.of("カレー", "ガスタ"));
        NICKS.put("リズリー", List.of("ズリー"));
        NICKS.put("クロノシア", List.of("ノシア", "シア"));
    }

    private final PayloadTrie<String> trie;

    private static final Database db = I.make(Database.class);

    private Nicknames() {

        PayloadTrieBuilder builder = PayloadTrie.builder().ignoreOverlaps();
        for (String name : db.uniqueSubNames()) {
            List<Unit> units = db.searchBySubName(name);

            register(builder, name, units);
            List<String> nicks = NICKS.get(name);
            if (nicks != null) {
                for (String nick : nicks) {
                    register(builder, nick, units);
                }
            }
        }

        FULL_NICKS.entrySet().forEach(e -> {
            String name = e.getKey();
            db.searchByFullName(name).ifPresent(unit -> {
                for (String nick : e.getValue()) {
                    register(builder, nick, List.of(unit));
                }
            });
        });

        builder.addKeyword("もりたん", "https://kuromojiya.sakura.ne.jp/aigis.htm");

        // 一般名詞の一部にキャラ名が含まれている場合にリンクを無効にするために
        // 名詞自体を登録しておく防衛戦略
        builder.addKeyword("バッファー", "");
        builder.addKeyword("レンジ", "");
        builder.addKeyword("ジレンマ", "");
        builder.addKeyword("チャレンジ", "");
        builder.addKeyword("ランキング", "");
        builder.addKeyword("ゴブリン", "");
        builder.addKeyword("リンク", "");
        builder.addKeyword("直リン", "");
        builder.addKeyword("リンゴ", "");
        builder.addKeyword("パリン", "");
        builder.addKeyword("リンカー", "");
        builder.addKeyword("リンチ", "");
        builder.addKeyword("リング", "");
        builder.addKeyword("クリリン", "");
        builder.addKeyword("マーリン", "");
        builder.addKeyword("アリーナ", "");
        builder.addKeyword("タンバリン", "");
        builder.addKeyword("ダーリン", "");
        builder.addKeyword("リンパ", "");
        builder.addKeyword("ガソリン", "");
        builder.addKeyword("バファリン", "");
        builder.addKeyword("マリン", "");
        builder.addKeyword("グリセリン", "");
        builder.addKeyword("インスリン", "");
        builder.addKeyword("ガンスリンガー", "");
        builder.addKeyword("キリン", "");
        builder.addKeyword("ダージリン", "");
        builder.addKeyword("リンガーハット", "");
        builder.addKeyword("リンダ", "");
        builder.addKeyword("オリンピック", "");
        builder.addKeyword("プリン", "");
        builder.addKeyword("アドレナリン", "");
        builder.addKeyword("ドーンブリンガー", "");
        builder.addKeyword("セーラー", "");
        builder.addKeyword("デミウルゴス", "");
        builder.addKeyword("コマンダー", "");
        builder.addKeyword("コマンド", "");
        builder.addKeyword("4コマ", "");
        builder.addKeyword("3コマ", "");
        builder.addKeyword("2コマ", "");
        builder.addKeyword("1コマ", "");
        builder.addKeyword("４コマ", "");
        builder.addKeyword("３コマ", "");
        builder.addKeyword("２コマ", "");
        builder.addKeyword("１コマ", "");
        builder.addKeyword("スーパー", "");
        builder.addKeyword("スーツ", "");
        builder.addKeyword("スーファミ", "");
        builder.addKeyword("スープ", "");
        builder.addKeyword("シレン", "");
        builder.addKeyword("フリーレン", "");
        builder.addKeyword("フレンド", "");
        builder.addKeyword("フレンズ", "");
        builder.addKeyword("レンチン", "");
        builder.addKeyword("レンジ", "");
        builder.addKeyword("レンコン", "");
        builder.addKeyword("アズレン", "");
        builder.addKeyword("サイレン", "");
        builder.addKeyword("スレンダー", "");
        builder.addKeyword("ブレンド", "");
        builder.addKeyword("フレンチ", "");
        builder.addKeyword("トレンド", "");
        builder.addKeyword("ギレン", "");
        builder.addKeyword("レンガ", "");
        builder.addKeyword("レンズ", "");
        builder.addKeyword("レント", "");
        builder.addKeyword("レンチ", "");
        builder.addKeyword("レンタカー", "");
        builder.addKeyword("カレンダー", "");
        builder.addKeyword("ハガレン", "");
        builder.addKeyword("ペアレンツ", "");
        builder.addKeyword("レンタル", "");
        builder.addKeyword("バレンタイン", "");
        builder.addKeyword("バンズ", "");
        builder.addKeyword("アドバンス", "");
        builder.addKeyword("バンク", "");
        builder.addKeyword("バンダイ", "");
        builder.addKeyword("バンデット", "");
        builder.addKeyword("バンデッド", "");
        builder.addKeyword("バンカー", "");
        builder.addKeyword("バンディット", "");
        builder.addKeyword("カバン", "");
        builder.addKeyword("イチバン", "");
        builder.addKeyword("バンプ", "");
        builder.addKeyword("アドバンテージ", "");
        builder.addKeyword("バンダナ", "");
        builder.addKeyword("ミョウバン", "");
        builder.addKeyword("バンコク", "");
        builder.addKeyword("バンド", "");
        builder.addKeyword("ヘブバン", "");
        builder.addKeyword("アバン", "");
        builder.addKeyword("バンバン", "");
        builder.addKeyword("ビッグバン", "");
        builder.addKeyword("バンザイ", "");
        builder.addKeyword("バンジー", "");
        builder.addKeyword("サバンナ", "");
        builder.addKeyword("バンされ", "");
        builder.addKeyword("バンする", "");
        builder.addKeyword("バンブー", "");
        builder.addKeyword("バンパー", "");
        builder.addKeyword("ミニバン", "");
        builder.addKeyword("ウォーキング", "");
        builder.addKeyword("バーガーキング", "");
        builder.addKeyword("バイキング", "");
        builder.addKeyword("パーキング", "");
        builder.addKeyword("ストッキング", "");
        builder.addKeyword("キングゲイナー", "");
        builder.addKeyword("キングダム", "");
        builder.addKeyword("キングスライム", "");
        builder.addKeyword("キングダムハーツ", "");
        builder.addKeyword("クッキング", "");
        builder.addKeyword("ムシキング", "");
        builder.addKeyword("スパンキング", "");
        builder.addKeyword("マスキング", "");
        builder.addKeyword("キングギドラ", "");
        builder.addKeyword("ライオンキング", "");
        builder.addKeyword("ハッキング", "");
        builder.addKeyword("コイキング", "");
        builder.addKeyword("ブレイキング", "");
        builder.addKeyword("トラッキング", "");
        builder.addKeyword("キングサイズ", "");
        builder.addKeyword("シロップ", "");
        builder.addKeyword("スシロー", "");
        builder.addKeyword("シロモノ", "");
        builder.addKeyword("シロクマ", "");
        builder.addKeyword("ケンシロウ", "");
        builder.addKeyword("シロアリ", "");
        builder.addKeyword("シロウ", "");
        builder.addKeyword("シロッコ", "");
        builder.addKeyword("アメリカ", "");
        builder.addKeyword("イギリス", "");
        builder.addKeyword("フランス", "");
        builder.addKeyword("ドイツ", "");
        builder.addKeyword("イタリア", "");
        builder.addKeyword("スペイン", "");
        builder.addKeyword("ロシア", "");
        builder.addKeyword("ポルトガル", "");
        builder.addKeyword("アジア", "");
        builder.addKeyword("アフリカ", "");
        builder.addKeyword("ダンまち", "");
        builder.addKeyword("ダンジョン", "");
        builder.addKeyword("ダンボール", "");
        builder.addKeyword("ダンディ", "");
        builder.addKeyword("ダンサー", "");
        builder.addKeyword("ダンシング", "");
        builder.addKeyword("ダンベル", "");
        builder.addKeyword("ダンナ", "");
        builder.addKeyword("ダンス", "");
        builder.addKeyword("モダン", "");
        builder.addKeyword("ラスダン", "");
        builder.addKeyword("ダンガン", "");
        builder.addKeyword("ダンバイン", "");
        builder.addKeyword("ダンターグ", "");
        builder.addKeyword("ダンテ", "");
        builder.addKeyword("ビダン", "");
        builder.addKeyword("ダンク", "");
        builder.addKeyword("ダントツ", "");
        builder.addKeyword("ダンクーガ", "");
        builder.addKeyword("サキュバス", "");
        builder.addKeyword("ウルトラ", "");
        builder.addKeyword("デミウル", "");
        builder.addKeyword("ウルゴス", "");
        builder.addKeyword("ウルフ", "");
        builder.addKeyword("ウルティマ", "");
        builder.addKeyword("ソウル", "");
        builder.addKeyword("ベラート", "");
        builder.addKeyword("バイドース", "");
        builder.addKeyword("クルスタ", "");
        builder.addKeyword("シノギ", "");
        builder.addKeyword("フトモモ", "");
        builder.addKeyword("ヘプバン", "");
        builder.addKeyword("マーキング", "");
        builder.addKeyword("バッファ", "");
        builder.addKeyword("バファー", "");

        this.trie = builder.build();
    }

    private static void register(PayloadTrieBuilder builder, String name, List<Unit> units) {
        units.stream().filter(u -> u.nameJ.startsWith("ちび")).forEach(u -> {
            builder.addKeyword("ちび" + name, "/character/" + u.nameJ + "/");
        });

        units.stream().filter(u -> !u.season.isEmpty()).forEach(u -> {
            for (Attribute season : u.season) {
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
            builder.addKeyword("無印" + name, "/character/" + u.nameJ + "/");
        });

        if (units.size() == 1) {
            Unit unit = units.getFirst();
            if (unit.nameJ.equals(unit.subNameJ) && db.searchBySubName(unit.subNameJ).size() > 1) {
                builder.addKeyword(name, "/character/?q=" + unit.nameJ);
            } else {
                builder.addKeyword(name, "/character/" + unit.nameJ + "/");
            }
        } else if (units.size() == 2) {
            Unit first = units.getFirst();
            Unit last = units.getLast();
            if (first.nameJ.startsWith("ちび") || first.nameJ.endsWith("（白）")) {
                builder.addKeyword(name, "/character/" + last.nameJ + "/");
            } else if (last.nameJ.startsWith("ちび") || last.nameJ.endsWith("（白）")) {
                builder.addKeyword(name, "/character/" + first.nameJ + "/");
            } else {
                builder.addKeyword(name, "/character/?q=" + first.subNameJ);
            }
        } else {
            builder.addKeyword(name, "/character/?q=" + units.getFirst().subNameJ);
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

    public void parse(String input, Consumer<PayloadEmit<String>> process) {
        trie.parseText(input).forEach(emit -> {
            process.accept(emit);
        });
    }
}
