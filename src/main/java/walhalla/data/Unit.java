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

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import kiss.I;
import kiss.JSON;
import walhalla.Astro;
import walhalla.data.lint.Proofreader;
import walhalla.util.WebPage;

public class Unit {

    public int id;

    public int seq;

    public String image;

    public String icon;

    public String imageAW;

    public String iconAW;

    public String image2A;

    public String icon2A;

    public String image2B;

    public String icon2B;

    public String name;

    public String nameJ;

    public String nameJEUC;

    public String subNameJ;

    public Gender gender;

    public Rarity rarity;

    public PlaceType place;

    public int year;

    public String artist;

    public String body;

    public List<String> ability;

    public List<String> abilityAW;

    public List<String> skill;

    public int reuse;

    public int init;

    public List<String> skillAW;

    public int reuseAW;

    public int initAW;

    public List<String> affection = new ArrayList<>();

    public boolean hero;

    public Stats stats;

    public Stats stats1;

    public Stats stats2A;

    public Stats stats2B;

    public List<Attribute> attributes = new ArrayList();

    public Map<String, Effect> effects = new HashMap();

    public List<Attribute> season = new ArrayList();

    public List<Attribute> affiliation = new ArrayList();

    public List<Attribute> race = new ArrayList();

    public List<Attribute> military = new ArrayList();

    public List<String> bounus100 = new ArrayList();

    public List<String> bounus150 = new ArrayList();

    public List<Integer> tier = new ArrayList();

    private boolean disableAW;

    public List<Stats> stats() {
        List<Stats> list = new ArrayList<>();
        if (stats != null) list.add(stats);
        if (stats1 != null) list.add(stats1);
        if (stats2A != null) list.add(stats2A);
        if (stats2B != null) list.add(stats2B);
        return list;
    }

    /**
     * 指定した名前に紐づくデータを取得します。キャッシュが有効な場合はキャッシュを返し、
     * 期限切れや未取得の場合はサーバーから取得してキャッシュします。
     *
     * @param name データ名（例: "キャラクター名/サブページ"）
     * @return データの文字列
     */
    private static String sourceByName(String name) {
        int index = name.indexOf("/");
        String characterName = index == -1 ? name : name.substring(0, index);
        long ttl = 14 * 24 * 60 * 60 * 1000 * (Astro.FORCE_UPDATE.contains(characterName) ? -1 : 1);
        return WebPage
                .fetchText("https://aigis.fandom.com/api.php?action=query&prop=revisions&titles=" + name + "&rvslots=main&rvprop=content&format=json", ttl);
    }

    void parseWikiCharacterDataByName(String name) {
        JSON json = I.json(sourceByName(name)).find("query", "pages", "*", "revisions", "*", "slots", "main").getFirst();

        parseWikiCharacterData(name, json.toString());
    }

    void parseWikiCharacterData(String name, String text) {
        this.name = name;

        WikiText wiki = new WikiText(text);
        wiki.peekSection("Unit infobox", () -> {
            wiki.peekKV("gender", value -> gender = value.toUpperCase().equals("MALE") ? Gender.男性 : Gender.女性);
            wiki.peekKV("rank", value -> {
                rarity = switch (value) {
                case "Iron" -> Rarity.鉄;
                case "Bronze" -> Rarity.銅;
                case "Silver" -> Rarity.銀;
                case "Gold" -> Rarity.金;
                case "Platinum" -> Rarity.白;
                case "Sapphire" -> Rarity.青;
                case "Black" -> Rarity.黒;
                default -> throw new IllegalStateException("Unknown rank: " + value);
                };
            });
            if (rarity == null) rarity = Rarity.王子;

            wiki.peekKV("disableaw", value -> disableAW = value.equals("y") || value.equals("yes") || value.equals("1"));
            wiki.peekKV("jpname", value -> {
                int start = value.indexOf("<br"); // support <br> and <br />
                if (start == -1) {
                    nameJ = value.strip();
                } else {
                    nameJ = value.substring(0, start).strip();
                }
                String resoleved = nameJ;
                if (disableAW) {
                    // Ephtra (Final Battle Black)があるので(Black)ではダメ
                    if (name.endsWith("Black)")) {
                        resoleved += "【黒英傑】";
                    } else if (name.endsWith("Platinum)")) {
                        resoleved += "【白金英傑】";
                    }
                }
                nameJEUC = URLEncoder.encode(resoleved, Charset.forName("EUC-JP")).replace("+", "%20");
            });
            wiki.peekKV("artist", value -> {
                artist = TextParser.extractArtistName(value);
            });
            wiki.peekKV("body", value -> body = value);
        });

        ToIntFunction<String> reuser = value -> {
            int index = value.indexOf("<br>");
            return Integer.parseInt(index == -1 ? value : value.substring(0, index).strip());
        };

        wiki.peekSection("Skill awakening item", () -> {
            wiki.peekKV("reuse", value -> {
                reuse = reuser.applyAsInt(value);
                init = switch (rarity) {
                case 黒 -> 1;
                case 白 -> reuse / 2;
                default -> reuse / 3 * 2;
                };
            });
            wiki.peekKV("awReuse", value -> {
                reuseAW = reuser.applyAsInt(value);
                initAW = switch (rarity) {
                case 黒 -> 5;
                case 白 -> reuse / 2;
                default -> reuse / 3 * 2;
                };
            });
        });

        Consumer<String> parseAffection = value -> {
            int start = value.indexOf("<!--");
            int end = value.indexOf("-->", start);
            if (start != -1) {
                String message = value.substring(start + 4, end).strip();
                if (!message.equals("ダミー")) {
                    affection.add(message);
                }
            }
        };

        wiki.peekSection("Quote table", () -> {
            wiki.peekKV("quote1", parseAffection);
            wiki.peekKV("quote2", parseAffection);
            wiki.peekKV("quote3", parseAffection);
            wiki.peekKV("quote4", parseAffection);
            wiki.peekKV("quote5", parseAffection);
            wiki.peekKV("quote6", parseAffection);
            wiki.peekKV("quote7", parseAffection);
            wiki.peekKV("quote8", parseAffection);
            wiki.peekKV("quote9", parseAffection);
            wiki.peekKV("quote10", parseAffection);
        });

        wiki.peekSection("Gallery", () -> {
            parseImage(wiki, name, "Icon.png", value -> icon = value);
            parseImage(wiki, name, "AW Icon.png", value -> iconAW = value);
            parseImage(wiki, name, "AW2 Icon.png", value -> icon2A = icon2B = value);
            parseImage(wiki, name, "AW2v1 Icon.png", value -> icon2A = value);
            parseImage(wiki, name, "AW2v2 Icon.png", value -> icon2B = value);
            parseImage(wiki, name, "Render.png", value -> image = value);
            parseImage(wiki, name, "AW Render.png", value -> imageAW = value);
            parseImage(wiki, name, "AW2 Render.png", value -> image2A = image2B = value);
            parseImage(wiki, name, "AW2v1 Render.png", value -> image2A = value);
            parseImage(wiki, name, "AW2v2 Render.png", value -> image2B = value);

            // 特例処理
            if (name.equals("Martan")) {
                image = calculateImagePath("Martan_Render.png");
                imageAW = calculateImagePath("Martan_AW_Render.png");
            } else if (name.equals("Farangis")) {
                icon = calculateImagePath("Farangis_Icon.png");
                iconAW = calculateImagePath("Farangis_AW_Icon.png");
            } else if (name.equals("Arslan")) {
                icon = calculateImagePath("Arslan_Icon.png");
                image = calculateImagePath("Arslan_Render.png");
            } else if (name.equals("Chibi Rino")) {
                icon = calculateImagePath("Chibi_Rino_Icon.png");
                image = calculateImagePath("Chibi_Rino_Render.png");
            } else if (name.equals("Chibi Sophie")) {
                icon = calculateImagePath("Chibi_Sophie_Icon.png");
                image = calculateImagePath("Chibi_Sophie_Render.png");
            } else if (name.equals("King")) {
                icon = calculateImagePath("King_Icon.png");
                iconAW = calculateImagePath("King_AW_Icon.png");
                image = calculateImagePath("King_Render.png");
                imageAW = calculateImagePath("King_AW_Render.png");
            } else if (name.equals("Zilva")) {
                icon = calculateImagePath("Zilva_Icon.png");
                iconAW = calculateImagePath("Zilva_AW_Icon.png");
                image = calculateImagePath("Zilva_Render.png");
                imageAW = calculateImagePath("Zilva_AW_Render.png");
            } else if (name.equals("Hawk-Winged Birdman Soldier")) {
                icon = calculateImagePath("Hawk-Winged_Birdman_Soldier_Icon.png");
                image = calculateImagePath("Hawk-Winged_Birdman_Soldier_Render.png");
            } else if (name.equals("Neve (Okyu Cosplay)")) {
                image = calculateImagePath("Neve_(Okyu_Cosplay)_Render.png");
                imageAW = calculateImagePath("Neve_(Okyu_Cosplay)_AW_Render.png");
            }
        });
    }

    private void parseImage(WikiText wiki, String name, String prefix, Consumer<String> setter) {
        String fileName = name.replace(" ", "_") + "_" + prefix.replace(" ", "_");
        if (wiki.peek(fileName)) {
            setter.accept(calculateImagePath(fileName));
            return;
        }

        fileName = name + " " + prefix;
        if (wiki.peek(fileName)) {
            setter.accept(calculateImagePath(fileName));
            return;
        }

        fileName = name + "_" + prefix.replace(" ", "_");
        if (wiki.peek(fileName)) {
            setter.accept(calculateImagePath(fileName));
            return;
        }

        fileName = name.replace(" ", "_") + " " + prefix;
        if (wiki.peek(fileName)) {
            setter.accept(calculateImagePath(fileName));
            return;
        }

        int index = name.indexOf("(");
        if (index != -1) {
            String suffix = name.substring(index);
            name = name.substring(0, index); // don't trim

            fileName = name + suffix.replace(" ", "_") + "_" + prefix.replace(" ", "_");
            if (wiki.peek(fileName)) {
                setter.accept(calculateImagePath(fileName));
                return;
            }
        }
    }

    void parseWikiStatsByName(String name) {
        JSON json = I.json(sourceByName(name + "/stats")).find("query", "pages", "*", "revisions", "*", "slots", "main").getFirst();

        parseWikiStats(json.toString());
    }

    void parseWikiStats(String text) {
        WikiText wiki = new WikiText(text);
        wiki.peekKV("race", value -> assign(Attribute.of(value)));
        wiki.peekKV("affiliation", value -> assign(Attribute.of(value)));
        wiki.peekKV("seasonal", value -> assign(Attribute.of(value)));
        wiki.peekKV("cat4", value -> assign(Attribute.of(value)));
        wiki.peekKV("cat5", value -> assign(Attribute.of(value)));

        // 追加属性は実際にはないので特別扱い
        if (name != null) {
            if (name.endsWith("(Swimsuit)")) assign(Attribute.Swimsuit);
            if (name.endsWith("(Yukata)")) assign(Attribute.Yukata);
            if (name.endsWith("(Festival)")) assign(Attribute.Festival);
            if (name.endsWith("Cosplay)")) assign(Attribute.Cosplay);
        }

        wiki.peekKV("hero", value -> hero = value.equals("y"));
        // heroプロパティの記載がない場合は、属性から判定する
        if (attributes.contains(Attribute.Hero)) hero = true;

        stats = new Stats();
        stats.parseWikiStats("c1", text);
        stats.icon = icon;;
        stats.image = image;

        wiki.peekKV("awaken", value -> {
            if (value.equals("y")) {
                stats1 = new Stats();
                stats1.parseWikiStats("c3", text);
                stats1.icon = iconAW == null ? icon : iconAW;
                stats1.image = imageAW == null ? image : imageAW;
            }
        });
        wiki.peekKV("awaken2A", value -> {
            if (value.equals("y")) {
                stats2A = new Stats();
                stats2A.parseWikiStats("c4", text);
                stats2A.icon = icon2A == null ? iconAW : icon2A;
                stats2A.image = image2A == null ? imageAW : image2A;
            }
        });
        wiki.peekKV("awaken2B", value -> {
            if (value.equals("y")) {
                stats2B = new Stats();
                stats2B.parseWikiStats("c5", text);
                stats2B.icon = icon2B == null ? iconAW : icon2B;
                stats2B.image = image2B == null ? imageAW : image2B;
            }
        });
        if (hero) stats = null;
        if (stats != null) stats.profession.parseWikiProfessionData(this);
        if (stats1 != null) stats1.profession.parseWikiProfessionData(this);
        if (stats2A != null) stats2A.profession.parseWikiProfessionData(this);
        if (stats2B != null) stats2B.profession.parseWikiProfessionData(this);

        UnaryOperator<String> bonusParser = value -> {
            value = value.trim();
            int start = value.indexOf("{{");
            int end = value.indexOf("}}", start);
            if (start != -1 && end != -1) {
                start = value.lastIndexOf("|", end) + 1;
                value = value.substring(start, end) + value.substring(end + 2).strip();
            }

            return value.replace("'", "")
                    .replace("ATK", "攻撃力")
                    .replace("DEF", "防御力")
                    .replace("MR", "魔法耐性")
                    .replace("Range", "射程")
                    .replace("Cost", "コスト")
                    .replace("PRC", "貫通確率")
                    .replace("PEV", "物理攻撃回避")
                    .replace("SCD", "再使用時間")
                    .replace("CD", "再使用時間")
                    .replace("PAD", "攻撃硬直")
                    .replace("SDI", "スキル効果時間");
        };

        wiki.peekKV("100AffBonus ", value -> {
            for (String bonus : value.split("<br>")) {
                bounus100.add(bonusParser.apply(bonus));
            }
        });
        wiki.peekKV("150AffBonus", value -> {
            for (String bonus : value.split("<br>")) {
                bounus150.add(bonusParser.apply(bonus));
            }
        });
    }

    private void assign(Attribute attribute) {
        if (attribute.type == AttributeType.種族) {
            if (!race.contains(attribute)) {
                race.add(attribute);
            }
        } else if (attribute.type == AttributeType.兵種) {
            if (!military.contains(attribute)) {
                military.add(attribute);
            }
        } else if (attribute.type == AttributeType.所属) {
            if (!affiliation.contains(attribute)) {
                affiliation.add(attribute);
            }
        } else if (attribute.type == AttributeType.季節) {
            if (!season.contains(attribute)) {
                season.add(attribute);
            }
        } else {
            attributes.add(attribute);
        }
    }

    private static Map<String, JSON> DB;

    private static synchronized Map<String, JSON> getCharacterDB() {
        if (DB == null) {
            DB = I.json("https://raw.githubusercontent.com/aigis1000secretary/AigisTools/refs/heads/master/AigisLoader/CharaDatabase.json")
                    .find("*")
                    .stream()
                    .collect(Collectors.toMap(x -> x.text("name"), x -> x));

            fixName(DB);
            fixStatus(DB);
        }
        return DB;
    }

    private static void fixName(Map<String, JSON> DB) {
        // =========================================
        // DBが提供する名前にはいくつかの問題があるため、修正を行う。
        // =========================================
        // 1. 「夏の月下冥神へカティエ」の名前を修正
        // ひらがなの「へ」をカタカナの「ヘ」に変更
        JSON json = DB.get("夏の月下冥神へカティエ");
        json.set("name", "夏の月下冥神ヘカティエ");
        json.set("subName", "ヘカティエ");
        DB.put("夏の月下冥神ヘカティエ", json);

        // 2. 「聖なる技巧兵ドリー」の名前を修正
        // 「技工兵」を「技巧兵」に変更
        json = DB.get("聖なる技工兵ドリー");
        json.set("name", "聖なる技巧兵ドリー");
        DB.put("聖なる技巧兵ドリー", json);
    }

    private static void fixStatus(Map<String, JSON> CARD) {
        // =========================================
        // DBが提供するステータスにはいくつかの問題があるため、修正を行う。
        // =========================================
        JSON json = CARD.get("秩序の亜神ラビリス");
        json.set("skill", json.text("skill").replace("<DEF>", "2.5"));

        json = CARD.get("帝国掘削教官ミュレ");
        json.set("skill_aw", json.text("skill_aw").replace("<DEF>", "2.5"));

        json = CARD.get("ぬりかべカゴメ");
        json.set("skill_aw", json.text("skill_aw").replace("<DEF>", "2"));

        json = CARD.get("上杉謙信");
        json.set("skill", json.text("skill").replace("<ATK>", "2.5"));

        json = CARD.get("初代帝国騎士団長ヘイズル");
        json.set("skill", json.text("skill").replace("<ATK>", "2"));

        json = CARD.get("帝国剣闘士アグナ");
        json.set("skill", json.text("skill").replace("<ATK>", "1.8"));
    }

    void parseAigisLoader() {
        String name = nameJ;

        if (name == null) {
            return;
        }

        if (attributes.contains(Attribute.Hero)) {
            name = name.replace("（白）", "").replace("（黒）", "");

            if (rarity == Rarity.黒) {
                name += "【黒英傑】";
            } else if (rarity == Rarity.白) {
                name += "【白金英傑】";

            }
        }

        JSON json = getCharacterDB().get(name);
        if (json != null) {
            subNameJ = json.text("subName").replace("ちび", "");
            ability = parseSkill(json.text("ability"), "▹", nameJ + "のアビリティ");
            abilityAW = parseSkill(json.text("ability_aw"), "▸", nameJ + "の覚醒アビリティ");

            if (abilityAW.isEmpty() && rarity.isRare() && !ability.isEmpty()) {
                abilityAW.addAll(ability);
            }

            skill = parseSkill(json.text("skill"), "▹", nameJ + "のスキル");
            skillAW = parseSkill(json.text("skill_aw"), "▸", nameJ + "の覚醒スキル");
        } else {
            throw new IllegalStateException("Cannot find unit data for " + nameJ + ". Please check the name or update the database.");
        }
    }

    private static List<String> parseSkill(String text, String separator, String desc) {
        List<String> skills = new ArrayList();
        for (String description : text.split(separator)) {
            if (!description.isBlank()) {
                String[] skill = description.split("\n");
                if (2 <= skill.length) {
                    skills.add(skill[0]);
                    skills.add(Proofreader.fix(skill[1], desc));
                }
            }
        }
        return skills;
    }

    private static Map<String, JSON> CARD;

    private static synchronized Map<String, JSON> getCardDB() {
        if (CARD == null) {
            CARD = new HashMap();
            String js = I
                    .http("https://raw.githubusercontent.com/aigis1000secretary/AigisTools/refs/heads/master/html/script/rawCardsList.js", String.class)
                    .to()
                    .acquire();

            I.json(js.substring(js.indexOf("["))).find("*").forEach(json -> CARD.put(json.text("name"), json));
            fixName(CARD);
        }
        return CARD;
    }

    void parseAigisTool() {
        JSON json = getCardDB().get(nameJ);
        if (json != null) {
            id = json.get(int.class, "id");
            place = PlaceType.values()[json.get(int.class, "placeType")];
            year = json.get(int.class, "year");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return I.express("""
                {nameJ} ({subNameJ} : {name} : {id}) 性別:{gender} レア:{rank} 属性:{attributes} {place} {year} サイズ:{body} アーティスト:{artist} ({artistLink})
                {#stats}
                第一覚醒   {stats}
                {/stats}
                {#stats2A}
                第二覚醒A {stats2A}
                {/stats2A}
                {#stats2B}
                第二覚醒B {stats2B}
                {/stats2B}
                好感度 {bounus100} {bounus150}
                スキル\t{skill}\t再使用:{reuse}
                覚醒スキル\t{skillAW}\t再使用:{reuseAW}
                セリフ\t{affection}
                """, this);
    }

    private static String calculateImagePath(String fileName) {
        fileName = fileName.replace(" ", "_");

        String hash = md5(fileName);
        return "https://static.wikia.nocookie.net/aigis/images/" + hash.charAt(0) + "/" + hash.substring(0, 2) + "/" + fileName;
    }

    /**
     * nameからMD5ハッシュ値（16進文字列）を算出するユーティリティメソッド。
     */
    private static String md5(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Analyzes the additional attributes of the unit.
     */
    void analyzeEffect() {
        for (int i = 1; i < skill.size(); i += 2) {
            skill.set(i, Ability.parse(skill.get(i), effects));
        }
        for (int i = 1; i < skillAW.size(); i += 2) {
            skillAW.set(i, Ability.parse(skillAW.get(i), effects));
        }

        for (int i = 1; i < ability.size(); i += 2) {
            ability.set(i, Ability.parse(ability.get(i), effects));
        }
        for (int i = 1; i < abilityAW.size(); i += 2) {
            abilityAW.set(i, Ability.parse(abilityAW.get(i), effects));
        }

        for (Stats stats : I.list(stats, stats1, stats2A, stats2B)) {
            if (stats != null) {
                stats.profession.description = Ability.parse(stats.profession.description, effects);
            }
        }
    }

    /**
     * @return
     */
    public UnitMeta asMeta() {
        UnitMeta meta = new UnitMeta();
        meta.seq = seq;
        attributes.forEach(attr -> meta.attrs.add(attr.nameJ));
        race.forEach(attr -> meta.attrs.add(attr.nameJ));
        season.forEach(attr -> meta.attrs.add(attr.nameJ));
        affiliation.forEach(attr -> meta.attrs.add(attr.nameJ));
        military.forEach(attr -> meta.attrs.add(attr.nameJ));
        effects.forEach((key, value) -> meta.attrs.add(key));
        meta.attrs.add(rarity.name());
        meta.attrs.add(place.name());
        meta.attrs.add(String.valueOf(year));
        meta.attrs.add(artist);
        meta.attrs.add(gender.name());
        stats().forEach(stats -> meta.attrs.add(stats.profession.nameJ));

        return meta;
    }

}