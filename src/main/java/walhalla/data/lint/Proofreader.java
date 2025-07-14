/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.data.lint;

public class Proofreader {

    public static final Linter LINTER = new Linter()
            // Normalization rules
            .normalize("ＨＰ", "HP")
            .normalize("攻防", "攻撃力・防御力")
            .normalize("攻撃と防御", "攻撃力・防御力")
            .normalize("毒、状態異常", "毒・状態異常")

            // Fixing rules
            .addRule("配置し、た瞬間", "配置した瞬間")
            .addRule("HPが0になっても", "\nHPが0になっても")
            .addRule("、HPが0になっても", "\nHPが0になっても")
            .addRule("攻撃する毎に", "\n攻撃する毎に")
            .addRule("攻撃毎に", "\n攻撃毎に")
            .addRule("遠距離攻撃に専念", "遠距離攻撃に専念\n")
            .addRule("行う", "行う\n")
            .addRule("行う\nトークンを", "行うトークンを")
            .addRule("自動発動+効果時間無限", "\n自動発動+効果時間無限")
            .addRule("【所持効果】", "\n所持効果：")
            .addRule("地上に降りる", "地上に降りる\n")
            .addRule("飛行ユニットを優先して攻撃", "\n飛行ユニットを優先して攻撃\n")
            .addRule("自身か味方が", "\n自身か味方が")
            .addRule("HP回復を受けられない", "HP回復を受けられない\n")
            .addRule("同名のユニットは", "\n同名のユニットは")
            .addRule("同系クラスは編成に1人まで", "\n同系クラスは編成に1人まで\n")
            .addRule("悪天候の影響を無効化", "\n悪天候の影響を無効化\n")
            .addRule("深海の悪影響を無効", "\n深海の悪影響を無効\n")
            .addRule("深海の影響を受けない", "\n深海の影響を受けない\n")
            .addRule("天界の悪影響を受けない", "\n天界の悪影響を受けない\n")
            .addRule("魚人を含む水棲の敵に対して", "\n魚人を含む水棲の敵に対して")
            .addRule("魔界でも能力が低下しない", "\n魔界でも能力が低下しない\n")
            .addRule("魔界と天界でも能力が低下しない", "\n魔界と天界でも能力が低下しない\n")
            .addRule("死亡した時、撤退扱いとなる", "\n死亡した時、撤退扱いとなる\n")
            .addRule("死亡しても短時間後で復活", "\n死亡しても短時間後で復活\n")
            .addRule("死亡しても一定時間後に復活", "\n死亡しても一定時間後に復活\n")
            .addRule("出撃メンバーにいるだけで", "\n出撃メンバーにいるだけで")
            .addRule("出撃人数に含まれず1人のみ編成可", "出撃人数に含まれず1人のみ編成可\n")
            .addRule("自身が死亡時", "\n自身が死亡時")
            .addRule("自身の毒", "\n自身の毒")
            .addRule("使役可能", "使役可能\n")
            .addRule("物理攻撃を回避する", "物理攻撃を回避する\n")
            .addRule("ブロックした敵全員を攻撃できる", "ブロックした敵全員を攻撃")
            .addRule("ブロックした敵全員を攻撃", "ブロックした敵全員を攻撃\n")
            .addRule("ブロックした全敵を攻撃", "\nブロックした敵全員を攻撃\n")
            .addRule("トークンは出撃数に含まれない", "\nトークンは出撃数に含まれない\n")
            .addRule("攻撃後の待ち時間を少し短縮", "\n攻撃後の待ち時間を少し短縮\n")
            .addRule("他者からHP回復を受けられない", "\n他者からHP回復を受けられない\n")
            .addRule("自身が死亡した時撤退として扱う", "\n自身が死亡した時撤退として扱う\n")
            .addRule("自身に対する敵からの遠距離攻撃の優先度を下げる", "\n自身に対する敵からの遠距離攻撃の優先度を下げる\n")
            .addRule("スキルを使用すると一定時間封印を解除", "\nスキルを使用すると一定時間封印を解除\n")
            .addRule("状態異常を完全に無効化", "状態異常を完全に無効化\n")
            .addRule("　", "\n")

            // Fixing rules for specific patterns
            .addRegex("編成([中|時])", "\n編成$1")
            .addRegex("配置([中|時])", "\n配置$1")
            .addRegex("(非?)スキル(発動)?(中|時)", "\n$1スキル$2$3")
            .addRegex("出撃([中|時])", "\n出撃$1")
            .addRegex("([^+])効果時間無限", "$1\n効果時間無限")
            .addRegex("([^、ず])敵の遠距離攻撃の対象にならない", "$1\n敵の遠距離攻撃の対象にならない\n")
            .addRegex("(\\d)体までの敵を足止めできる", "$1体までの敵を足止めできる\n")
            .addRegex("\\)([^\\(で])", ")\n$1");

    public static String fix(String text, String desc) {
        text = LINTER.fix(text, desc);

        String z = text.replace("\n、", "\n").replace("、\n", "\n").replaceAll("(\\r\\n|\\r|\\n| )+", "\n").strip();

        return z;
    }

}