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

import kiss.Decoder;
import kiss.Encoder;

public enum Attribute {
    Angel("天使"),

    Artillery("砲兵"),

    Beastfolk("獣人"),

    Birdfolk("鳥人"),

    Bowman("弓兵"),

    Cavalry("騎兵"),

    Celestial("天界人"),

    Chibi("ちび"),

    Christmas("クリスマス"),

    Clergy("聖職者"),

    DarkElf("ダークエルフ"),

    DeepSea("深海"),

    Demon("デーモン"),

    DesertCountry("砂漠の国"),

    Dragon("ドラゴン"),

    Dragonfolk("竜人"),

    Dwarf("ドワーフ"),

    EasternCountry("東の国"),

    EggHunt("エッグハント"),

    Elf("エルフ"),

    FlowerCountry("華の国"),

    Flying("飛行"),

    Giant("巨人"),

    Goblin("ゴブリン"),

    God("神"),

    Gunner("銃士"),

    HalfDarkElf("ハーフダークエルフ"),

    HalfDemon("ハーフデーモン"),

    HalfElf("ハーフエルフ"),

    HalfGod("半神"),

    Halloween("ハロウィン"),

    Heavy("重装"),

    Hermit("仙人"),

    Hero("英傑"),

    HotSprings("温泉"),

    Human("人間"),

    JuneBride("花嫁"),

    Kingdom("王国"),

    KingdomofPars("パルス王国"),

    Machine("機械"),

    Magician("魔術師"),

    Makai("魔界"),

    Merfolk("魚人"),

    Nendoroid("ねんどろいど"),

    NewYears("お正月"),

    Noble("高貴"),

    None("なし"),

    Orc("オーク"),

    School("学園"),

    SevenDeadlySins("七つの大罪"),

    Spirit("精霊"),

    Summer("サマー"),

    Undead("アンデッド"),

    Underworld("冥界人"),

    ValentinesDay("バレンタイン"),

    Vampire("ヴァンパイア"),

    WhiteEmpire("白の帝国"),

    Youkai("妖怪");

    public String nameJ;

    private Attribute(String nameJ) {
        this.nameJ = nameJ;
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
