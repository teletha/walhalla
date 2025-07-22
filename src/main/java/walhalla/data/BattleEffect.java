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

public enum BattleEffect {

    同系クラスは編成に1人まで,

    遠近両配置マスに配置可能,

    敵の遠距離攻撃を引き付ける,

    敵の遠距離攻撃の対象にならない,

    自身に対する敵からの遠距離攻撃の優先度を下げる,

    自動発動,

    自動発動スキルの対象外,

    出撃中の全員がスキルを自動使用,

    手動でスキル終了("手動でスキルを終了"),

    効果時間無限,

    スキル発動時にコストを消費,

    出撃コストが徐々に増加("出撃コストが徐々に回復", "出撃コストを徐々に増加"),

    魔法攻撃("攻撃魔法"),

    遠距離魔法攻撃("遠距離の魔法攻撃"),

    範囲魔法攻撃,

    遠距離範囲魔法攻撃("遠距離の範囲魔法攻撃"),

    貫通攻撃("貫通属性"),

    遠距離貫通攻撃("遠距離の貫通攻撃"),

    範囲貫通攻撃,

    物理攻撃,

    遠距離攻撃("遠距離物理攻撃", "遠距離の物理攻撃"),

    即死攻撃,

    継続ダメージ,

    徐々にＨＰ回復,

    状態異常を回復,

    回復対象にならない("HP回復を受けられない"),

    攻撃後の待ち時間を短縮("攻撃後の待ち時間をやや短縮"),

    攻撃後の待ち時間を増加,

    敵の移動速度を下げ,

    所持効果,

    バリア,

    動きを止める,

    発動中はダメージを受けない,

    その場で復活("その場に復活"),

    再出撃可能,

    再出撃可能にする,

    ブロックした敵全員を攻撃,

    出撃人数に含まれない("出撃人数に含まれず", "出撃数に含まれず"),

    毒及び状態異常を無効("毒・状態異常を無効化"),

    天界の影響を受けない,

    魔界の影響を受けない,

    深海の影響を受けない,

    悪天候の影響を受けない,

    吹雪の影響を受けない,

    出撃メンバーにいるだけで,

    撤退扱い,

    飛行ユニットを優先攻撃("飛行ユニットを優先して攻撃"),

    味方遠距離ユニットが優先的に攻撃,

    撤退時にコストが回復("撤退時にコストが[10-100/5]%回復"),

    出撃コストを下げる("出撃コストを[1-10/1]下げる"),

    麻痺;

    private static PayloadTrie<Effect> trie;

    private BattleEffect() {
        this(new String[0]);
    }

    private BattleEffect(StepEffect effect) {
        this();
    }

    private BattleEffect(String... alias) {
        BattleEffectParser.builder.addKeyword(name(), new Effect(name()));
        for (String a : alias) {
            BattleEffectParser.builder.addKeyword(a, new Effect(name()));
        }
    }

    public static String parse(String input, Map<String, Effect> effects) {
        input = input.replaceAll("<a\\b[^>]*>(.*?)</a>", "$1");

        if (trie == null) {
            trie = BattleEffectParser.builder.build();
        }

        StringBuilder result = new StringBuilder();
        Collection<PayloadToken<Effect>> tokens = trie.tokenize(input);

        for (PayloadToken<Effect> token : tokens) {
            PayloadEmit<Effect> emit = token.getEmit();
            if (emit != null) {
                Effect effect = emit.getPayload();
                effects.put(effect.type, effect);
                result.append("<a href='")
                        .append("/character/?アビリティ1=" + effect.type)
                        .append("'>")
                        .append(token.getFragment())
                        .append("</a>");
            } else {
                result.append(token.getFragment());
            }
        }
        return result.toString();
    }
}
