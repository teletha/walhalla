/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.topics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.XML;
import walhalla.data.Attribute;
import walhalla.data.Database;
import walhalla.data.Nicknames;
import walhalla.data.Rarity;
import walhalla.data.Unit;
import walhalla.open2ch.OpenThreadCollector;
import walhalla.open2ch.Res;
import walhalla.util.WebPage;

/**
 * TierCalculatorは、各種データソースからユニットのトレンドやスコアを集計し、
 * ユニットごとのTier（評価）を計算・表示するクラスです。
 * <ul>
 * <li>calculateTrend(): オープンスレッドからユニット名の出現頻度を集計します。</li>
 * <li>calculateMajin(): 魔神イベントの集計データを取得し、ユニットごとのスコアに反映します。</li>
 * <li>calculateTower(): 各種塔イベントのスコアを集計し、ユニットごとのスコアに反映します。</li>
 * <li>show(): 集計結果をスコア順に表示します。</li>
 * </ul>
 * 本クラスはSingletonとして管理されます。
 */
@Managed(Singleton.class)
public class TierCalculator {
    /**
     * ユニット名ごとのTier情報を保持するマップ。
     */
    private Map<String, Tier> tiers = new HashMap();

    /**
     * ユニット情報を取得するためのデータベースインスタンス。
     */
    private Database db = I.make(Database.class);

    public TierCalculator calculate() {
        calculateTrend();
        calculateMajin();
        calculateTower();

        for (Unit unit : db) {
            Tier tier = tiers.get(unit.nameJ);
            if (tier != null) {
                unit.tier.add(tier.trend);
                unit.tier.add(tier.tower);
                unit.tier.add(tier.majin);
            }
        }

        return this;
    }

    /**
     * オープンスレッドからユニット名の出現頻度を集計し、Tier情報に反映します。
     */
    public void calculateTrend() {
        Nicknames nicknames = I.make(Nicknames.class);
        Set<String> ignoreSeasons = Set.of("コマ", "鬼刃姫", "エフネ", "エフトラ", "ルチア", "コウメ", "クゥイル", "メーア", "ミネット", "レギーナ", "コレット", "モーティマ");

        OpenThreadCollector.findAll().to(thread -> {
            for (Res res : thread.comments) {
                String input = res.decodedBody();

                nicknames.parse(input, token -> {
                    String name = token.getPayload();
                    if (!name.isEmpty()) {
                        if (name.startsWith("/character/?q=")) {
                            name = name.substring(14);

                            List<Unit> units = db.searchBySubName(name);
                            if (ignoreSeasons.contains(name)) {
                                byName(units.get(0).nameJ).addWord(input);
                            } else {
                                for (Unit unit : units) {
                                    if (unit.nameJ.endsWith("（白）") || (unit.hero && unit.rarity == Rarity.白)) {
                                        continue; // 白金英傑は除外
                                    }

                                    if (unit.season.contains(Attribute.Festival)) {
                                        continue; // 祭りユニットは除外
                                    }

                                    byName(unit.nameJ).addWord(input);
                                }
                            }

                        } else if (name.startsWith("/character/")) {
                            name = name.substring(11, name.length() - 1);

                            byName(name).addWord(input);
                        }
                    }
                });
            }
        });

        // トップのポイントが2200を超えている場合、全ユニットのポイントを減少させて調整
        int maxTrend = tiers.values().stream().mapToInt(t -> t.trend).max().orElse(0);
        if (maxTrend > 2200) {
            double ratio = (2200d / (maxTrend - 37));
            for (Tier tier : tiers.values()) {
                tier.trend = (int) Math.round(tier.trend * ratio);
            }
        }
    }

    /**
     * 魔神イベントの集計データを取得し、ユニットごとのTierスコアに反映します。
     */
    public void calculateMajin() {
        Set<String> unique = new HashSet();
        XML xml = WebPage.fetchXML("https://autocounter.net/aigis/majin/index.html");
        for (XML box : xml.find("div.unit_display_box")) {
            String text = box.lastChild().text().trim() + box.firstChild().text().trim();
            if (unique.add(text)) {
                String eventPath = box.lastChild().lastChild().attr("href");
                String eventURL = "https://autocounter.net/aigis/majin/" + eventPath;

                if (eventPath.endsWith("top.html")) {
                    XML event = WebPage.fetchXML(eventURL);
                    XML highlevel = event.find("div.unit_display_box").last();
                    String levelPath = highlevel.lastChild().lastChild().attr("href");
                    collectUnit("https://autocounter.net/aigis/majin/" + eventPath.replace("top.html", "") + levelPath);
                } else if (eventPath.endsWith("all.html")) {
                    collectUnit(eventURL);
                }
            }
        }
    }

    /**
     * 魔神イベントの個別ページからユニットのスコアを集計します。
     *
     * @param url 集計対象のイベントページURL
     */
    private void collectUnit(String url) {
        XML root = WebPage.fetchXML(url);
        String date = root.find("div.majin_summary-item:nth-child(4)").text().trim();
        date = date.substring(0, 10);
        if (date.endsWith("～")) date = date.substring(0, date.length() - 2) + "01";

        LocalDate now = LocalDate.now();
        LocalDate eventDate = DateTimeFormatter.ofPattern("yyyy/MM/dd").parse(date, LocalDate::from);

        if (eventDate.isBefore(now.minusYears(3))) {
            return;
        }

        // Calculate decay factor based on event age
        long daysSinceEvent = eventDate.until(now, ChronoUnit.DAYS);
        double decayFactor = 1.0; // Default: no decay for events between 1 to 1.5 years

        if (daysSinceEvent <= 365) { // Recent events within 1 year: boost by 20%
            decayFactor = 1.2;
        } else if (daysSinceEvent > 547) { // More than 1.5 years old: apply decay
            // Linear decay from 100% (at 1.5 years) to 60% (at 3 years)
            long maxDecayDays = 365 * 3; // 3 years (1095 days)
            long decayStartDays = 547; // 1.5 years
            long decayRangeDays = maxDecayDays - decayStartDays; // Range from 1.5 years to 3 years

            if (daysSinceEvent >= maxDecayDays) {
                decayFactor = 0.3; // Minimum 60% for 3+ year old events
            } else {
                // Linear interpolation: 1.0 at 1.5 years, 0.6 at 3 years
                long excessDays = daysSinceEvent - decayStartDays;
                decayFactor = 1.0 - (0.7 * excessDays / decayRangeDays);
            }
        }

        for (XML unit : root.find("div.unit_display_box")) {
            String top = unit.firstChild().text().trim();
            int count = Integer.parseInt(top.substring(top.indexOf("：") + 1, top.indexOf("回")).trim());

            String name = unit.lastChild().text().trim();
            if (name.endsWith("B")) {
                name = name.substring(0, name.length() - 1); // 黒英傑
            } else if (name.endsWith("P")) {
                name = name.substring(0, name.length() - 1) + "(白)"; // 白金英傑
            }

            // Apply decay factor to the count before adding to majin score
            byName(name).majin += Math.max(1, Math.floor(count * decayFactor));
        }
    }

    /**
     * 各種塔イベントのスコアを集計し、ユニットごとのTierスコアに反映します。
     */
    public void calculateTower() {
        calculateTower("https://wikiwiki.jp/aigiszuki/%E8%8B%B1%E5%82%91%E3%81%AE%E5%A1%94/%E5%8B%95%E7%94%BB", 150000);
        calculateTower("https://wikiwiki.jp/aigiszuki/%E7%B5%B1%E5%B8%A5%E3%81%AE%E5%A1%94/%E5%8B%95%E7%94%BB", 150000);
        calculateTower("https://wikiwiki.jp/aigiszuki/%E6%82%AA%E9%9C%8A%E3%81%AE%E5%A1%94/%E5%8B%95%E7%94%BB", 110000);
    }

    /**
     * 指定した塔イベントページからスコアを集計し、ユニットごとのTierスコアに反映します。
     *
     * @param url 集計対象の塔イベントページURL
     * @param minimumScore 集計対象とする最小スコア
     */
    private void calculateTower(String url, int minimumScore) {
        XML xml = WebPage.fetchXML(url);
        for (XML row : xml.find("tr")) {
            List<XML> columns = I.signal(row.find("td")).toList();
            int size = columns.size();
            if (4 < size) {
                int score = Integer.parseInt(columns.get(1).text().trim());
                if (score <= minimumScore) {
                    continue;
                }

                List<String> used = new ArrayList();
                for (int i = 3; i < size - 1; i++) {
                    String name = columns.get(i).text().trim();
                    if (!name.isEmpty() && !name.equals("-")) {
                        if (name.endsWith("(黒)")) {
                            name = name.substring(0, name.length() - 3);
                        } else if (name.endsWith("(プラチナ)")) {
                            name = name.substring(0, name.length() - 6) + "(白)";
                        } else if (name.endsWith("(白)")) {
                            // do nothing
                        } else {
                            int index = name.indexOf("(");
                            if (index != -1) {
                                name = name.substring(0, index);
                            } else {
                                index = name.indexOf("（");
                                if (index != -1) {
                                    name = name.substring(0, index);
                                } else {
                                    // do nothing
                                }
                            }
                        }
                        used.add(name);
                    }
                }

                if (!used.isEmpty()) {
                    int point = (int) (score / (Math.pow(used.size(), 2) + 4));
                    for (String name : used) {
                        byName(name).tower += point / 1000;
                    }
                }
            }
        }
    }

    /**
     * 集計したTier情報をスコア順に表示します。
     * 一定条件（スコアが低い、ちび・白金・アンナ等）は除外されます。
     */
    public void show() {
        // countの降順でkeyを表示
        tiers.entrySet().stream().sorted(Map.Entry.<String, Tier> comparingByValue().reversed()).forEach(entry -> {
            String key = entry.getKey();
            Tier tier = entry.getValue();

            if (tier.score() < 300) {
                return; // Skip low score tiers
            }

            if (key.startsWith("ちび")) {
                return; // Skip chibi units
            }

            // if (key.endsWith("（白）")) {
            // return; // Skip 白金英傑
            // }

            if (key.endsWith("アンナ") || key.startsWith("アンナ")) {
                return; // Skip アンナ
            }

            System.out.println(key + " : " + tier.score());
        });
    }

    /**
     * 指定したユニット名に対応するTierインスタンスを取得します。
     * 未登録の場合は新規作成します。
     *
     * @param name ユニット名
     * @return Tierインスタンス
     */
    private Tier byName(String name) {
        return tiers.computeIfAbsent(name, n -> {
            Tier tier = new Tier();
            tier.name = n;
            return tier;
        });
    }
}
