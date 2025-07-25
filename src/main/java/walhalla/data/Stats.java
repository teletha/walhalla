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

import kiss.I;

/**
 * Represents the statistics of a character or unit in the game. This class provides fields for
 * various attributes such as health points, attack, defense, and other abilities. It also includes
 * methods to parse these statistics from a wiki text format.
 */
public class Stats {

    /**
     * The class or type of the unit.
     */
    public Profession profession;

    public String icon;

    public String image;

    /**
     * The maximum health points of the unit.
     */
    public int hp;

    /**
     * The maximum attack value of the unit.
     */
    public int atk;

    /**
     * The maximum defense value of the unit.
     */
    public int def;

    /**
     * The magic resistance value of the unit.
     */
    public int mr;

    /**
     * The range of the unit's attacks or abilities.
     */
    public int range;

    /**
     * The number of enemies the unit can block simultaneously.
     */
    public int block;

    /**
     * The maximum cost of deploying the unit.
     */
    public int cost;

    /**
     * The minimum cost of deploying the unit.
     */
    public int costMin;

    /**
     * The range of the unit's skill.
     */
    public int rangeSkill;

    /**
     * The range of the unit's skill after awakening.
     */
    public int rangeSkillAW;

    /**
     * The cost modifier for the unit's abilities.
     */
    public int abilityCostMod;

    /**
     * The attack modifier for the unit's abilities.
     */
    public float abilityAtkMod;

    public float abilityDefMod;

    /**
     * Parses the statistics of the unit from a wiki text format.
     *
     * @param prefix The prefix used for the keys in the wiki text.
     * @param text The wiki text containing the statistics.
     */
    void parseWikiStats(String prefix, String text) {
        WikiText wiki = new WikiText(text);

        wiki.peekKV(prefix + "Class", value -> profession = I.make(ProfessionManager.class).findBy(value));
        wiki.peekKV(prefix + "MaxHp", value -> hp = parseInt(value));
        wiki.peekKV(prefix + "MaxAtk", value -> atk = parseInt(value));
        wiki.peekKV(prefix + "MaxDef", value -> def = parseInt(value));
        wiki.peekKV(prefix + "MinCost", value -> costMin = parseInt(value));
        wiki.peekKV(prefix + "MaxCost", value -> cost = parseInt(value, costMin));
        wiki.peekKV(prefix + "Resist", value -> mr = parseInt(value));
        wiki.peekKV(prefix + "Range", value -> range = parseInt(value));
        wiki.peekKV(prefix + "Block", value -> block = parseInt(value));
        wiki.peekKV(prefix + "SRange", value -> rangeSkill = parseInt(value));
        wiki.peekKV(prefix + "SAWRange", value -> rangeSkillAW = parseInt(value));
        wiki.peekKV(prefix + "ACostMod", value -> abilityCostMod = parseInt(value));
        wiki.peekKV(prefix + "AAtkMod", value -> abilityAtkMod = parseModifier(value));
        wiki.peekKV(prefix + "ADefMod", value -> abilityDefMod = parseModifier(value));
    }

    private static float parseModifier(String value) {
        if (value.startsWith("*")) {
            value = value.substring(1);
        } else if (value.endsWith("%")) {
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("x")) {
            value = value.substring(0, value.length() - 1);
        }
        return Float.parseFloat(value);
    }

    /**
     * Parses an integer value from a string. If the string is empty, returns 0.
     *
     * @param value The string to parse.
     * @return The parsed integer value.
     */
    private static int parseInt(String value) {
        return parseInt(value, 0);
    }

    /**
     * Parses an integer value from a string. If the string is empty, returns 0.
     *
     * @param value The string to parse.
     * @return The parsed integer value.
     */
    private static int parseInt(String value, int defaultValue) {
        if (value.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue; // Return default value if parsing fails
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return I.express("""
                {profession}
                HP: {hp}\tATK: {atk}\tDEF: {def}\tMR: {mr}\tRNG: {range}\tBLK: {block}\tCOST: {cost}({costMin})\tSRNG: {rangeSkill}\tSRNG(AW): {rangeSkillAW}\tACostMod: {abilityCostMod}\tAAtkMod: {abilityAtkMod}\tADefMod: {abilityDefMod}
                """, this)
                .trim();
    }
}