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

import java.util.ArrayList;
import java.util.List;

public class ProfessionGroup {

    public Profession normal = Profession.EMPTY;

    public Profession awaken = Profession.EMPTY;

    public Profession awaken2A = Profession.EMPTY;

    public Profession awaken2B = Profession.EMPTY;

    public List<Profession> asList() {
        List<Profession> list = new ArrayList<>();
        if (normal != Profession.EMPTY) list.add(normal);
        if (awaken != Profession.EMPTY) list.add(awaken);
        if (awaken2A != Profession.EMPTY) list.add(awaken2A);
        if (awaken2B != Profession.EMPTY) list.add(awaken2B);
        return list;
    }

    public ProfessionNameList asNameList() {
        ProfessionNameList list = new ProfessionNameList();
        for (Profession profession : asList()) {
            list.add(profession.nameJ);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ProfessionGroup [normal=" + normal.nameJ + ", awaken=" + awaken.nameJ + ", awaken2A=" + awaken2A.nameJ + ", awaken2B=" + awaken2B.nameJ + "]";
    }

    /**
     * For serialization of profession names.
     */
    public static class ProfessionNameList extends ArrayList<String> {
    }
}
