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

public class Proofreader {

    public static String fix(String text) {
        String z = text.replaceAll("編成([中|時])", "\n編成$1")
                .replaceAll("配置([中|時])", "\n配置$1")
                .replaceAll("(非?)スキル([中|時])", "\n$1スキル$2")
                .replaceAll("出撃([中|時])", "\n出撃$1")
                .replace("攻撃する毎に", "\n攻撃する毎に")
                .replace("攻撃毎に", "\n攻撃毎に")
                .replace("行う", "行う\n")
                .replace("自動発動+効果時間無限", "\n自動発動+効果時間無限")
                .replaceAll("([^+])効果時間無限", "$1\n効果時間無限")
                .replace("【所持効果】", "\n【所持効果】")
                .replace("地上に降りる", "地上に降りる\n")
                .replace("飛行ユニットを優先して攻撃", "\n飛行ユニットを優先して攻撃\n")
                .replace("自身か味方が", "\n自身か味方が")
                .replace("、ＨＰが0になっても", "\nHPが0になっても")
                .replace("ＨＰが0になっても", "\nHPが0になっても")
                .replace("同名のユニットは", "\n同名のユニットは")
                .replace("同系クラスは編成に1人まで", "\n同系クラスは編成に1人まで\n")
                .replace("悪天候の影響を無効化", "\n悪天候の影響を無効化\n")
                .replace("深海の悪影響を無効", "\n深海の悪影響を無効\n")
                .replace("深海の影響を受けない", "\n深海の影響を受けない\n")
                .replace("天界の悪影響を受けない", "\n天界の悪影響を受けない\n")
                .replace("魚人を含む水棲の敵に対して", "\n魚人を含む水棲の敵に対して")
                .replace("魔界でも能力が低下しない", "\n魔界でも能力が低下しない\n")
                .replace("魔界と天界でも能力が低下しない", "\n魔界と天界でも能力が低下しない\n")
                .replace("死亡した時、撤退扱いとなる", "\n死亡した時、撤退扱いとなる\n")
                .replace("死亡しても短時間後で復活", "\n死亡しても短時間後で復活\n")
                .replace("死亡しても一定時間後に復活", "\n死亡しても一定時間後に復活\n")
                .replace("出撃メンバーにいるだけで", "\n出撃メンバーにいるだけで")
                .replace("出撃人数に含まれず1人のみ編成可", "出撃人数に含まれず1人のみ編成可\n")
                .replaceAll("[^、]敵の遠距離攻撃の対象にならない", "\n敵の遠距離攻撃の対象にならない\n")
                .replace("自身が死亡時", "\n自身が死亡時")
                .replace("自身の毒", "\n自身の毒")
                .replaceAll("(\\d)体までの敵を足止めできる", "$1体までの敵を足止めできる\n")
                .replace("物理攻撃を回避する", "物理攻撃を回避する\n")
                .replace("ブロックした敵全員を攻撃できる", "ブロックした敵全員を攻撃")
                .replace("ブロックした敵全員を攻撃", "ブロックした敵全員を攻撃\n")
                .replace("ブロックした全敵を攻撃", "\nブロックした敵全員を攻撃\n")
                .replace("トークンは出撃数に含まれない", "\nトークンは出撃数に含まれない\n")
                .replace("攻撃後の待ち時間を少し短縮", "\n攻撃後の待ち時間を少し短縮\n")
                .replace("他者からHP回復を受けられない", "\n他者からHP回復を受けられない\n")
                .replace("自身が死亡した時撤退として扱う", "\n自身が死亡した時撤退として扱う\n")
                .replace("スキルを使用すると一定時間封印を解除", "\nスキルを使用すると一定時間封印を解除\n")
                .replace("状態異常を完全に無効化", "状態異常を完全に無効化\n")
                .replaceAll("\\)([^\\(])", ")\n$1")
                .replace("\n、", "\n")
                .replace("、\n", "\n")
                .replaceAll("(\\r\\n|\\r|\\n| )+", "\n")
                .strip();

        return z;
    }

}
