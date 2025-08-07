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
import java.util.Map;

import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadToken;
import org.ahocorasick.trie.PayloadTrie;

public enum Ability {

    同系クラスは編成に1人まで(AbilityCategory.編成),

    遠近両配置マスに配置可能(AbilityCategory.配置),

    敵の遠距離攻撃を引き付ける(AbilityCategory.配置),

    敵の遠距離攻撃の対象にならない(AbilityCategory.配置),

    敵からの遠距離攻撃の優先度を下げる(AbilityCategory.配置),

    自動発動(AbilityCategory.スキル),

    自動発動スキルの対象外(AbilityCategory.スキル),

    戦闘中1回しか使用できない(AbilityCategory.スキル),

    出撃中の全員がスキルを自動使用(AbilityCategory.スキル),

    手動でスキル終了(AbilityCategory.スキル, "手動でスキルを終了"),

    効果時間無限(AbilityCategory.スキル),

    スキル発動時にコストを消費(AbilityCategory.スキル),

    出撃コストが徐々に増加(AbilityCategory.コスト, "出撃コストが徐々に回復", "出撃コストを徐々に増加"),

    魔法攻撃(AbilityCategory.攻撃種別, "攻撃魔法"),

    遠距離魔法攻撃(AbilityCategory.攻撃種別, "遠距離の魔法攻撃"),

    範囲魔法攻撃(AbilityCategory.攻撃種別),

    遠距離範囲魔法攻撃(AbilityCategory.攻撃種別, "遠距離の範囲魔法攻撃"),

    貫通攻撃(AbilityCategory.攻撃種別, "貫通属性"),

    遠距離貫通攻撃(AbilityCategory.攻撃種別, "遠距離の貫通攻撃"),

    範囲貫通攻撃(AbilityCategory.攻撃種別),

    物理攻撃(AbilityCategory.攻撃種別),

    遠距離攻撃(AbilityCategory.攻撃種別, "遠距離物理攻撃", "遠距離の物理攻撃"),

    即死攻撃(AbilityCategory.攻撃種別),

    継続ダメージ(AbilityCategory.攻撃種別),

    徐々にＨＰ回復(AbilityCategory.回復),

    状態異常を回復(AbilityCategory.回復),

    回復対象にならない(AbilityCategory.回復, "HP回復を受けられない"),

    攻撃後の待ち時間を短縮(AbilityCategory.攻撃, "攻撃後の待ち時間をやや短縮"),

    攻撃後の待ち時間を増加(AbilityCategory.デバフ),

    敵の移動速度を下げ(AbilityCategory.デバフ),

    所持効果(AbilityCategory.その他),

    バリア(AbilityCategory.防御),

    防御に専念する(AbilityCategory.防御),

    回復に専念する(AbilityCategory.回復),

    遠距離攻撃に専念する(AbilityCategory.攻撃),

    敵に止めを刺さない(AbilityCategory.攻撃),

    動きを止める(AbilityCategory.デバフ),

    発動中はダメージを受けない(AbilityCategory.防御),

    その場で復活(AbilityCategory.死亡, "その場に復活"),

    再出撃可能(AbilityCategory.死亡),

    再出撃可能にする(AbilityCategory.死亡),

    ブロックした敵全員を攻撃(AbilityCategory.攻撃),

    出撃人数に含まれない(AbilityCategory.配置, "出撃人数に含まれず", "出撃数に含まれず"),

    毒及び状態異常を無効(AbilityCategory.無効, "毒・状態異常を無効化"),

    天界の影響を受けない(AbilityCategory.無効),

    魔界の影響を受けない(AbilityCategory.無効),

    深海の影響を受けない(AbilityCategory.無効),

    悪天候の影響を受けない(AbilityCategory.無効),

    吹雪の影響を受けない(AbilityCategory.無効),

    編成効果(AbilityCategory.編成),

    撤退として扱う(AbilityCategory.死亡),

    飛行ユニットを優先的に攻撃(AbilityCategory.攻撃),

    味方遠距離ユニットが優先的に攻撃(AbilityCategory.攻撃),

    スキル発動時に出撃コストが回復(AbilityCategory.コスト),

    敵を倒すと出撃コストが回復(AbilityCategory.コスト),

    撤退時に出撃コストが回復(AbilityCategory.コスト, "撤退時にコストが[10-100/5]%回復"),

    出撃コストが減少する(AbilityCategory.コスト, "出撃コストを[1-10/1]下げる"),

    麻痺(AbilityCategory.攻撃);

    private static PayloadTrie<Effect> trie;

    public final AbilityCategory type;

    private Ability(AbilityCategory type) {
        this(type, new String[0]);
    }

    private Ability(AbilityCategory type, StepEffect effect) {
        this(type, new String[0]);
    }

    private Ability(AbilityCategory type, String... alias) {
        this.type = type;
        AbilityParser.builder.addKeyword(name(), new Effect(name()));
        for (String a : alias) {
            AbilityParser.builder.addKeyword(a, new Effect(name()));
        }
    }

    public static String parse(String input, Map<String, Effect> effects) {
        if (input == null) {
            return null;
        }

        input = input.replaceAll("<a\\b[^>]*>(.*?)</a>", "$1");

        if (trie == null) {
            trie = AbilityParser.builder.build();
        }

        StringBuilder result = new StringBuilder();
        Collection<PayloadToken<Effect>> tokens = trie.tokenize(input);

        for (PayloadToken<Effect> token : tokens) {
            PayloadEmit<Effect> emit = token.getEmit();
            if (emit != null) {
                Effect effect = emit.getPayload();
                effects.put(effect.type, effect);
                result.append("<a href='/character/?アビリティ=").append(effect.type).append("'>").append(token.getFragment()).append("</a>");
            } else {
                result.append(token.getFragment());
            }
        }
        return result.toString();
    }
}
