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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kiss.I;
import walhalla.data.ProfessionGroup.ProfessionNameList;

public class UnitMetaInfo {

    private static final Collator collator = Collator.getInstance(Locale.JAPANESE);

    public List<PlaceType> 配置型 = List.of(PlaceType.近接, PlaceType.遠距離, PlaceType.遠近両用);

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
            .map(a -> a + ">>" + TextParser.getFullReading(a))
            .sort(collator)
            .toList();

    public List<String> 実装年 = I.signal(I.make(Database.class)).map(u -> String.valueOf(u.year)).distinct().reverse().toList();

    public List<String> 取得 = List.of("ガチャ", "配布");

    public List<String> アビリティ = I.signal(I.make(Database.class)).flatIterable(u -> u.effects.keySet()).distinct().sort(collator).toList();

    public Map<AbilityCategory, List<Ability>> 特性 = Stream.of(Ability.values())
            .collect(Collectors.groupingBy(x -> x.type, () -> new TreeMap(), Collectors.toList()));

    public List<ProfessionNameList> クラスセット = I.make(ProfessionManager.class)
            .findAllGroups()
            .stream()
            .filter(group -> !group.normal.nameJ.startsWith("ちび"))
            .map(ProfessionGroup::asNameList)
            .toList();
}
