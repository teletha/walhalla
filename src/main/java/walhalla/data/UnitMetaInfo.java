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

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import kiss.I;

public class UnitMetaInfo {

    private static final Collator collator = Collator.getInstance(Locale.JAPANESE);

    public List<PlaceType> 配置型 = List.of(PlaceType.近距離, PlaceType.遠距離, PlaceType.遠近);

    public List<Gender> 性別 = List.of(Gender.values());

    public List<Rarity> 希少度 = Stream.of(Rarity.values())
            .filter(rare -> rare != Rarity.王子 && rare != Rarity.鉄 && rare != Rarity.銅)
            .sorted(Comparator.reverseOrder())
            .toList();

    public List<String> 種族 = Stream.of(Attribute.values())
            .filter(a -> a.type == AttributeType.種族)
            .map(a -> a.nameJ)
            .sorted(collator)
            .toList();

    public List<String> 兵種 = Stream.of(Attribute.values())
            .filter(a -> a.type == AttributeType.兵種)
            .map(a -> a.nameJ)
            .sorted(collator)
            .toList();

    public List<String> 所属 = Stream.of(Attribute.values())
            .filter(a -> a.type == AttributeType.所属)
            .map(a -> a.nameJ)
            .sorted(collator)
            .toList();

    public List<String> 季節 = Stream.of(Attribute.values())
            .filter(a -> a.type == AttributeType.季節)
            .map(a -> a.nameJ)
            .sorted(collator)
            .toList();

    public List<String> 属性 = Stream.of(Attribute.values())
            .filter(a -> a.type == AttributeType.その他 && a != Attribute.None)
            .map(a -> a.nameJ)
            .sorted(collator)
            .toList();

    public List<String> アーティスト = I.signal(I.make(Database.class))
            .map(u -> u.artist)
            .skipNull()
            .skip(a -> a.isEmpty())
            .distinct()
            .sort(collator)
            .toList();

    public List<String> 実装年 = I.signal(I.make(Database.class)).map(u -> String.valueOf(u.year)).distinct().reverse().toList();

    public List<String> アビリティ = I.signal(I.make(Database.class)).flatIterable(u -> u.effects.keySet()).distinct().sort(collator).toList();

    public List<String> クラス = I.signal(I.make(Database.class))
            .map(u -> u.stats)
            .skipNull()
            .map(s -> s.profession.nameJ)
            .skip(name -> name.startsWith("ちび"))
            .distinct()
            .sort(collator)
            .toList();

    public List<String> クラス覚醒1 = I.signal(I.make(Database.class))
            .map(u -> u.stats1)
            .skipNull()
            .map(s -> s.profession.nameJ)
            .skip(name -> name.startsWith("ちび"))
            .distinct()
            .sort(collator)
            .toList();

    public List<String> クラス覚醒2 = I.signal(I.make(Database.class))
            .flatIterable(u -> I.list(u.stats2A, u.stats2B))
            .skipNull()
            .map(s -> s.profession.nameJ)
            .skip(name -> name.startsWith("ちび"))
            .distinct()
            .sort(collator)
            .toList();

    public List<Classes> クラスセット = I.signal(I.make(Database.class)).take(u -> u.rarity.isRare()).map(u -> {
        Classes classes = classMap.computeIfAbsent(u.stats().getFirst().profession.nameJ, key -> new Classes());
        if (u.stats != null) classes.normal = u.stats.profession.nameJ;
        if (u.stats1 != null) classes.awaken1 = u.stats1.profession.nameJ;
        if (u.stats2A != null) classes.awaken2A = u.stats2A.profession.nameJ;
        if (u.stats2B != null) classes.awaken2B = u.stats2B.profession.nameJ;
        return classes;
    }).skipNull().skip(name -> name.normal.startsWith("ちび")).distinct().toList();

    private static final Map<String, Classes> classMap = new HashMap();

    public static class Classes {
        public String normal = "";

        public String awaken1 = "";

        public String awaken2A = "";

        public String awaken2B = "";
    }
}
