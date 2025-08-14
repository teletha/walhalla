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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import kiss.XML;
import psychopath.File;
import psychopath.Locator;
import walhalla.Astro;
import walhalla.util.WebPage;

/**
 * Manages the collection and storage of {@link Unit} data.
 * <p>
 * This class loads, builds, and stores unit data from external sources such as the Aigis Wiki.
 * It provides methods to retrieve units by name or rarity, and ensures the unit database is
 * up-to-date.
 * </p>
 */
@SuppressWarnings("serial")
@Managed(Singleton.class)
public class Database extends ArrayList<Unit> implements Storable<Database> {

    /**
     * Constructs a UnitManager instance. Loads unit data from storage or rebuilds it if outdated.
     */
    private Database() {
        File file = Locator.file(locate());

        if (file.lastModifiedMilli() < System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 * (Astro.FORCE_UPDATE.isEmpty() ? 1 : -1)) {
            I.info("Unit data is outdated. Rebuilding...");
            build();
            store();
        } else {
            I.info("Loading unit data from storage...");
            restore();
        }
        fixNotImplementedUnits();
        I.info("Loaded unit data.");
    }

    /**
     * Builds the unit list by parsing data from the Aigis Wiki and other sources.
     * Updates the internal list and sorts units by ID.
     */
    public void build() {
        clear();

        Set<String> recorder = new HashSet();

        for (String name : names()) {
            try {
                if (name.equals("Prince") || name.startsWith("Prince (") || name.equals("Chibi Prince")) {
                    continue; // Skip Prince units
                }

                Unit unit = new Unit();
                unit.parseWikiCharacterDataByName(name);

                if (Astro.NOT_IMPLEMENTED.contains(unit.nameJ)) {
                    continue; // Skip not implemented units
                }

                unit.parseWikiStatsByName(name);
                unit.parseAigisLoader();
                unit.parseAigisTool();
                unit.analyzeEffect();

                if (unit.name == null) {
                    throw new Error("Failed to parse unit data for: " + name);
                } else if (!unit.rarity.isColored()) {
                    continue; // Skip non-rare units
                }

                add(unit);

                if (!recorder.add(unit.nameJ)) {
                    // 英傑（白）は名前が被っているので少し弄る
                    unit.nameJ = unit.nameJ + "（白）";
                }
            } catch (Exception e) {
                I.error("Failed to parse unit data for: " + name, e);
                throw e;
            }
        }

        // Sort units by ID
        sort(Comparator.comparingInt(x -> x.id));
        int sequence = 0;
        for (Unit unit : this) {
            unit.seq = sequence++;
        }

        store();
        fixNotImplementedUnits();
    }

    private void fixNotImplementedUnits() {
        for (Unit unit : this) {
            if (Astro.NOT_IMPLEMENTED.contains(unit.nameJ)) {
                unit.image = null;
                unit.imageAW = null;
                unit.image2A = null;
                unit.image2B = null;
                unit.bounus100 = Collections.EMPTY_LIST;
                unit.bounus150 = Collections.EMPTY_LIST;
                for (Stats stats : I.list(unit.stats, unit.stats1, unit.stats2A, unit.stats2B)) {
                    if (stats != null) {
                        stats.atk = 0;
                        stats.def = 0;
                        stats.mr = 0;
                        stats.hp = 0;
                        stats.cost = 0;
                        stats.costMin = 0;
                        stats.range = 0;
                        stats.block = 0;
                        stats.image = null;
                    }
                }
            }
        }
    }

    private static List<String> names() {
        Set<String> recorder = new HashSet<>();
        List<String> names = new ArrayList();

        Consumer<XML> scan = img -> {
            String name = img.attr("data-image-name");
            if (name.length() > 0) {
                name = name.substring(0, name.length() - 8);
                if (name.endsWith(" AW ")) {
                    name = name.substring(0, name.length() - 4);
                }
                name = name.trim();

                if (name.equals("Ranged") || name.equals("Melee") || name.equals("Magic") || name.equals("Support")) {
                    return; // Skip generic unit types
                }

                if (recorder.add(name)) {
                    names.add(name.replace("&#39;", "'"));
                }
            }
        };

        long ttl = 12 * 60 * 60 * 1000 * (Astro.FORCE_UPDATE.isEmpty() ? 1 : -1);
        I.xml(WebPage.fetchText("https://aigis.fandom.com/wiki/Category:Female_Units", ttl)).element("img").forEach(scan);
        I.xml(WebPage.fetchText("https://aigis.fandom.com/wiki/Category:Male_Units", ttl)).element("img").forEach(scan);
        I.xml(WebPage.fetchText("https://aigis.fandom.com/wiki/Aigis_Wiki", ttl))
                .find("#New_Units")
                .parent()
                .nextUntil("p")
                .element("img")
                .forEach(scan);

        return names;
    }

    /**
     * {@inheritDoc}
     * Returns the path to the unit database file.
     */
    @Override
    public Path locate() {
        return Path.of(".data/db.json");
    }

    /**
     * Retrieves all units with the specified Japanese sub-name.
     *
     * @param name The Japanese sub-name to search for
     * @return A list of units with the given sub-name
     */
    public List<Unit> searchBySubName(String name) {
        return stream().filter(u -> u.subNameJ.equals(name)).toList();
    }

    public Optional<Unit> searchByFullName(String name) {
        return stream().filter(u -> u.nameJ.equals(name)).findFirst();
    }

    /**
     * Retrieves all units with the specified rarities.
     *
     * @param rare One or more rarities to filter by
     * @return A list of units matching the given rarities
     */
    public List<Unit> by(Rarity... rare) {
        Set<Rarity> rarities = Set.of(rare);

        return stream().filter(u -> rarities.contains(u.rarity)).toList();
    }

    public Set<String> uniqueSubNames() {
        Set<String> names = new HashSet<>();
        for (Unit unit : this) {
            names.add(unit.subNameJ);
        }
        return names;
    }
}