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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import kiss.Managed;
import kiss.Singleton;

@Managed(Singleton.class)
public class ProfessionManager {

    /** The registered profession groups. */
    private final Map<Profession, ProfessionGroup> groups = new TreeMap();

    /** The registered professions. */
    private final Map<String, Profession> professions = new HashMap();

    public Profession findBy(String name) {
        return professions.computeIfAbsent(name, key -> {
            Profession profession = new Profession();
            profession.name = key;
            return profession;
        });
    }

    /**
     * 
     */
    public Collection<ProfessionGroup> findAllGroups() {
        return groups.values();
    }

    /**
     * Registers a profession group to the manager.
     * 
     * @param unit
     */
    public ProfessionGroup registerGroup(Unit unit) {
        if (!unit.rarity.isRare()) {
            return null;
        }

        Stats stats = unit.stats != null ? unit.stats : unit.stats1;
        return groups.computeIfAbsent(stats.profession, key -> {
            ProfessionGroup created = new ProfessionGroup();
            if (unit.stats != null) created.normal = unit.stats.profession;
            if (unit.stats1 != null) created.awaken = unit.stats1.profession;
            if (unit.stats2A != null) created.awaken2A = unit.stats2A.profession;
            if (unit.stats2B != null) created.awaken2B = unit.stats2B.profession;
            return created;
        });
    }
}
