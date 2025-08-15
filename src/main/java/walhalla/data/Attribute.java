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

import static walhalla.data.AttributeType.*;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

public enum Attribute {
    Angel(種族, "天使"),

    Artillery(兵種, "砲兵"),

    Beastfolk(種族, "獣人"),

    Birdfolk(種族, "鳥人"),

    Bowman(兵種, "弓兵"),

    Cavalry(兵種, "騎兵"),

    Celestial(種族, "天界人"),

    Chibi(その他, "ちび"),

    Christmas(季節, "クリスマス", new String[] {"クリ", "サンタ"}),

    Clergy(兵種, "聖職者"),

    Cosplay(季節, "コスプレ", new String[] {"蛸", "タコ", "たこ", "城", "キャメ"}),

    DarkElf(種族, "ダークエルフ"),

    DeepSea(所属, "深海"),

    Demon(種族, "デーモン"),

    DesertCountry(所属, "砂漠の国"),

    Dragon(種族, "ドラゴン"),

    Dragonfolk(種族, "竜人"),

    Dwarf(種族, "ドワーフ"),

    EasternCountry(所属, "東の国"),

    EggHunt(季節, "エッグハント", new String[] {"エッグ", "バニー", "バニ"}),

    Elf(種族, "エルフ"),

    Festival(季節, "大祭"),

    FlowerCountry(所属, "華の国"),

    Flying(兵種, "飛行"),

    Giant(種族, "巨人"),

    Goblin(種族, "ゴブリン"),

    God(種族, "神"),

    Gunner(兵種, "銃士"),

    HalfDarkElf(種族, "ハーフダークエルフ"),

    HalfDemon(種族, "ハーフデーモン"),

    HalfElf(種族, "ハーフエルフ"),

    HalfGod(種族, "半神"),

    Halloween(季節, "ハロウィン", new String[] {"ハロ"}),

    Heavy(兵種, "重装"),

    Hermit(種族, "仙人"),

    Hero(その他, "英傑"),

    HotSprings(季節, "温泉", new String[] {"湯"}),

    Human(種族, "人間"),

    JuneBride(季節, "花嫁", new String[] {"嫁"}),

    Kingdom(所属, "王国"),

    KingdomofPars(所属, "パルス王国"),

    Machine(所属, "機械"),

    Magician(兵種, "魔術師"),

    Makai(所属, "魔界"),

    Merfolk(種族, "魚人"),

    Nendoroid(種族, "ねんどろいど"),

    NewYears(季節, "お正月", new String[] {"晴", "晴着", "着物", "新年", "正月"}),

    Noble(その他, "高貴"),

    None(その他, "なし"),

    Orc(種族, "オーク"),

    School(季節, "学園", new String[] {"学"}),

    SevenDeadlySins(所属, "七つの大罪"),

    Spirit(種族, "精霊"),

    Summer(季節, "サマー", new String[] {"夏"}),

    Swimsuit(季節, "水着", new String[] {"水", "ミズ"}),

    Undead(種族, "アンデッド"),

    Underworld(種族, "冥界人"),

    ValentinesDay(季節, "バレンタイン", new String[] {"バレ", "ヴァレ", "チョコ", "V", "Vt"}),

    Vampire(種族, "ヴァンパイア"),

    WhiteEmpire(所属, "白の帝国"),

    Youkai(種族, "妖怪"),

    Yukata(季節, "浴衣", new String[] {"ゆかた"});

    public final String nameJ;

    public final String[] seasonPrefix;

    public final AttributeType type;

    private Attribute(AttributeType type, String nameJ) {
        this(type, nameJ, new String[0]);
    }

    private Attribute(AttributeType type, String nameJ, String[] seasonalKeyword) {
        this.type = type;
        this.nameJ = nameJ;
        this.seasonPrefix = I.array(seasonalKeyword, nameJ);
    }

    public boolean isSeasonal() {
        return seasonPrefix.length > 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return nameJ;
    }

    public static Attribute of(String name) {
        name = name.replaceAll("[ '\\-]", "");

        for (Attribute affiliation : values()) {
            if (affiliation.name().equals(name) || affiliation.nameJ.equals(name)) {
                return affiliation;
            }
        }
        return None;
    }

    private static class Codec implements Encoder<Attribute>, Decoder<Attribute> {

        @Override
        public String encode(Attribute value) {
            return value.nameJ;
        }

        @Override
        public Attribute decode(String value) {
            return Attribute.of(value);
        }
    }
}
