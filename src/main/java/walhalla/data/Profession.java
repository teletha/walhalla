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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import kiss.I;
import kiss.XML;
import walhalla.Astro;
import walhalla.data.lint.Proofreader;

public class Profession implements Comparable<Profession> {

    private static final Collator collator = Collator.getInstance(Locale.JAPANESE);

    static final Profession EMPTY = new Profession() {

        {
            nameJ = "未実装";
        }

        @Override
        public String toString() {
            return "未実装";
        }
    };

    public String name;

    public String nameJ;

    public String description;

    public String type;

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Profession o) {
        return collator.compare(nameJ, o.nameJ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return I.express("""
                {nameJ}({name}) \t{description}
                """, this).trim();
    }

    void parseWikiProfessionData(Unit unit) {
        if (nameJ != null) {
            return; // Already parsed
        }

        if (unit.nameJ == null || Astro.NOT_IMPLEMENTED.contains(unit.nameJ)) {
            return; // No unit name available
        }

        String unitName = unit.nameJ.replace("（白）", "").replace("（黒）", "");
        if (unitName == null) {
            return; // No unit name available
        }

        if (unitName.equals("英傑の塔")) {
            unitName = "英傑の塔(ユニット)";
        }

        String source = Wiki.source("https://wikiwiki.jp/aigiszuki/" + unitName.replace("婚礼つむじ風 ", "婚礼つむじ風")
                .replace(" ", "%20"), 28L * 24 * 60 * 60 * 1000);
        XML xml = I.xml(source);

        XML columns = xml.find("h3:contains('クラス特性')");
        do {
            columns = columns.next();
        } while (!columns.hasClass("h-scrollable"));

        Deque<XML> rows = new ArrayDeque();
        columns.element("tbody").element("tr").forEach(tr -> {
            XML row = tr.element("td");
            if (row.attr("colspan").isEmpty()) {
                rows.add(row);
            }
        });

        if (unit.stats2A == null) {
            if (unit.stats2B == null) {
                if (unit.stats1 == null) {
                    set(unit.stats, rows.pollLast(), "通常");
                } else {
                    set(unit.stats1, rows.pollLast(), "第一覚醒");
                    set(unit.stats, rows.pollLast(), "通常");
                }
            } else {
                set(unit.stats2B, rows.pollLast(), "第二覚醒B");
                set(unit.stats1, rows.pollLast(), "第一覚醒");
                set(unit.stats, rows.pollLast(), "通常");
            }
        } else {
            if (unit.stats2B == null) {
                set(unit.stats2A, rows.pollLast(), "第二覚醒A");
                set(unit.stats1, rows.pollLast(), "第一覚醒");
                set(unit.stats, rows.pollLast(), "通常");
            } else {
                set(unit.stats2B, rows.pollLast(), "第二覚醒B");
                set(unit.stats2A, rows.pollLast(), "第二覚醒A");
                set(unit.stats1, rows.pollLast(), "第一覚醒");
                set(unit.stats, rows.pollLast(), "通常");
            }
        }

        if (unit.stats1 != null && unit.stats1.profession.description.isEmpty()) {
            unit.stats1.profession.description = unit.stats.profession.description;
        }

        I.make(ProfessionManager.class).registerGroup(unit);
    }

    private void set(Stats stats, XML row, String type) {
        if (row != null) {
            String name = row.first().text().trim();
            int index = name.indexOf("(");
            if (index > 0) name = name.substring(0, index).trim();

            XML description = row.first().next();
            description.element("br").text("");

            stats.profession.type = type;
            stats.profession.nameJ = name;
            stats.profession.description = Proofreader.fix(description.text().trim(), name + "のクラス特性");
        }
    }
}
