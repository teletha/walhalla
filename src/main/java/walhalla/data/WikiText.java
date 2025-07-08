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

import java.util.function.Consumer;

/**
 * A utility class for parsing and manipulating wiki text. This class provides methods to extract
 * key-value pairs, sections, and other structured data from a wiki text format.
 */
public class WikiText {

    /**
     * The raw wiki text to be parsed.
     */
    private String text;

    /**
     * The current position in the text for parsing.
     */
    private int current;

    /**
     * Constructs a WikiText instance with the given raw text.
     *
     * @param text The raw wiki text to be parsed.
     */
    public WikiText(String text) {
        this.text = text.replace("\\n", "\n");
    }

    /**
     * Finds a key-value pair in the wiki text and processes its value.
     *
     * @param key The key to search for.
     * @param value A consumer to process the value associated with the key.
     */
    public void findKV(String key, Consumer<String> value) {
        require("|" + key);
        require("=");
        value.accept(readLine());
    }

    /**
     * Peeks at a key-value pair in the wiki text without advancing the parsing position.
     *
     * @param key The key to search for.
     * @param value A consumer to process the value associated with the key.
     */
    public void peekKV(String key, Consumer<String> value) {
        context(() -> {
            if (optional("|" + key)) {
                require("=");

                try {
                    value.accept(readLine());
                } catch (Throwable e) {
                    throw new IllegalStateException("Failed to read value for key: [" + key + "] in " + text, e);
                }
            }
        });
    }

    /**
     * Peeks at a section in the wiki text without advancing the parsing position.
     *
     * @param text The section title to search for.
     * @param action A runnable to execute if the section is found.
     */
    public void peekSection(String text, Runnable action) {
        context(() -> {
            try {
                int start = require("{{", text);
                int end = require("}}");

                this.text = this.text.substring(start + 2 + text.length(), end);
                this.current = 0;

                action.run();
            } catch (IllegalStateException e) {
                try {
                    int start = require("<", text + ">");
                    int end = require("</", text + ">");

                    this.text = this.text.substring(start + 2 + text.length(), end);
                    this.current = 0;

                    action.run();
                } catch (IllegalStateException e2) {
                }
            }
        });
    }

    /**
     * Executes an action within a temporary parsing context.
     *
     * @param action The action to execute.
     */
    private void context(Runnable action) {
        String previousText = this.text;
        int precitousCurrent = this.current;

        try {
            action.run();
        } finally {
            this.text = previousText;
            this.current = precitousCurrent;
        }
    }

    /**
     * Requires the presence of a specific value in the text and advances the parsing position.
     *
     * @param value The value to search for.
     * @return The index of the value in the text.
     * @throws IllegalStateException If the value is not found.
     */
    private int require(String value) {
        int index = text.indexOf(value, current);
        if (index == -1) {
            throw new IllegalStateException("Cannot find value: [" + value + "] in " + text);
        } else {
            current = index + value.length();
            return index;
        }
    }

    /**
     * Requires the presence of a specific value in the text and advances the parsing position.
     *
     * @param value The value to search for.
     * @return The index of the value in the text.
     * @throws IllegalStateException If the value is not found.
     */
    private int require(String prefix, String type) {
        type = type.toLowerCase();

        root: while (true) {
            int index = text.indexOf(prefix, current);
            int prefixLength = prefix.length();
            if (index == -1 || index + prefixLength + type.length() > text.length()) {
                throw new IllegalStateException("Cannot find value: [" + prefix + type + "] in " + text);
            } else {
                // test starting with type (ignore case)
                for (int i = 0; i < type.length(); i++) {
                    if (Character.toLowerCase(text.charAt(index + prefixLength + i)) != type.charAt(i)) {
                        current = index + prefixLength + i;
                        continue root;
                    }
                }

                current = index + prefix.length();
                return index;
            }
        }
    }

    public boolean peek(String value) {
        return text.indexOf(value, current) != -1;
    }

    /**
     * Optionally checks for the presence of a specific value in the text without throwing an
     * exception.
     *
     * @param value The value to search for.
     * @return True if the value is found, false otherwise.
     */
    public boolean optional(String value) {
        int index = text.indexOf(value, current);
        if (index == -1) {
            return false;
        } else {
            current = index + value.length();
            return true;
        }
    }

    /**
     * Reads a line of text until the next delimiter ("|" or "\n").
     *
     * @return The line of text.
     */
    private String readLine() {
        int bar = text.indexOf("|", current);
        int breaks = text.indexOf("\n", current);
        int braceStart = text.indexOf("{{", current);
        if (braceStart != -1 && braceStart < bar) {
            bar = text.indexOf("|", text.indexOf("}}", bar) + 2);
        }

        int index;

        if (bar == -1) {
            if (breaks == -1) {
                index = text.length();
            } else {
                index = breaks;
            }
        } else {
            if (breaks == -1) {
                index = bar;
            } else {
                index = Math.min(bar, breaks);
            }
        }

        return text.substring(current, index).replace("\\t", "\t").strip();
    }
}