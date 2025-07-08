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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import kiss.XML;
import psychopath.File;
import psychopath.Locator;

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

        if (file.lastModifiedMilli() < System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) {
            I.info("Unit data is outdated. Rebuilding...");
            build();
            store();
        } else {
            restore();
        }
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
                unit.parseWikiStatsByName(name);
                unit.parseAigisLoader();
                unit.parseAigisTool();

                if (unit.name == null) {
                    throw new Error("Failed to parse unit data for: " + name);
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
    }

    private static List<String> names() {
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
                names.add(name.replace("&#39;", "'"));
            }
        };

        I.xml(Wiki.source("https://aigis.fandom.com/wiki/Category:Female_Units", 12 * 60 * 60 * 1000)).element("img").forEach(scan);
        I.xml(Wiki.source("https://aigis.fandom.com/wiki/Category:Male_Units", 12 * 60 * 60 * 1000)).element("img").forEach(scan);

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
    public List<Unit> byName(String name) {
        return stream().filter(u -> u.subNameJ.equals(name)).toList();
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