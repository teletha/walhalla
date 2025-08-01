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
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;
import walhalla.Astro;

/**
 * A utility class for caching data retrieved from a server. The cache is stored in a local
 * directory and is valid for up to 7 days. If the cache is expired or does not exist, the data is
 * fetched from the server and saved to the cache.
 */
public class Wiki {

    /**
     * The directory where cached data is stored.
     */
    private static final Directory CACHE_DIR = Locator.directory(".data/cache");

    /**
     * The timestamp of the last server request.
     */
    private static long lastRequestTime = 0;

    /**
     * Retrieves data associated with the given name. If a valid cache exists, it returns the cached
     * data. Otherwise, it fetches the data from the server, saves it to the cache, and returns it.
     *
     * @param name The name of the data to retrieve.
     * @return The data as a string.
     */
    public static String sourceByName(String name) {
        int index = name.indexOf("/");
        String characterName = index == -1 ? name : name.substring(0, index);
        long ttl = 14 * 24 * 60 * 60 * 1000 * (Astro.FORCE_UPDATE.contains(characterName) ? -1 : 1);
        return source("https://aigis.fandom.com/api.php?action=query&prop=revisions&titles=" + name + "&rvslots=main&rvprop=content&format=json", ttl);
    }

    /**
     * Retrieves data associated with the given name. If a valid cache exists, it returns the cached
     * data. Otherwise, it fetches the data from the server, saves it to the cache, and returns it.
     *
     * @param name The name of the data to retrieve.
     * @return The data as a string.
     */
    public static String source(String name, long cache) {
        String hash = String.valueOf(name.hashCode());
        File file = CACHE_DIR.file(hash);

        if (file.isPresent()) {
            long lastModified = file.lastModifiedMilli();
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastModified < cache) {
                return file.text();
            }
        }

        long interval = name.startsWith("https://wikiwiki.jp/aigiszuki/") ? 1000 : 250;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < interval) {
            try {
                Thread.sleep(interval - (currentTime - lastRequestTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        name = name.replace(" ", "%20").replace("(", "%28").replace(")", "%29").replace("'", "%27");

        System.out.println("Downloading " + name);
        String data = I.http(name, String.class).waitForTerminate().to().exact();

        file.text(data);
        lastRequestTime = System.currentTimeMillis();

        return data;
    }
}