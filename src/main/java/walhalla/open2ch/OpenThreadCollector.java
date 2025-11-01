/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.open2ch;

import java.util.ArrayDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kiss.I;
import kiss.Signal;
import walhalla.Astro;
import walhalla.util.Server;

/**
 * A utility class for managing and updating open2ch threads related to "Aigis".
 * 
 * <p>
 * This class performs the following operations:
 * <ul>
 * <li>Fetches a list of recent threads using the open2ch search service.</li>
 * <li>Determines whether each thread needs to be updated based on post count and last modified
 * time.</li>
 * <li>Launches the thread in a browser and collects the HTML source via a temporary local HTTP
 * server.</li>
 * <li>Saves the updated HTML source to local storage.</li>
 * </ul>
 */
public class OpenThreadCollector {

    /** Ensures that threads are only updated once per session. */
    private static boolean initialized;

    public static synchronized OpenThread findBy(String id) {
        int index = id.indexOf("-");
        int num = Integer.parseInt(id.substring(0, index));
        int threadId = Integer.parseInt(id.substring(index + 1));

        return new OpenThread(num, threadId);
    }

    public static synchronized void crawlByURL(int num, int id) {
        try {
            Server server = new Server();
            OpenThread thread = new OpenThread(num, id);
            String source = server.fetchSource("https://uni.open2ch.net/test/read.cgi/gameswf/" + id + "/");
            thread.parse(source);

            Thread.sleep(1000);

            server.shutdown();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Returns a signal (reactive stream) of all parsed thread data.
     * If this is the first call, it triggers the update process.
     * 
     * @return a Signal containing {@link OpenThread} objects
     */
    public static synchronized Signal<OpenThread> findAll() {
        if (initialized == false) {
            initialized = true;
            trail();
        }
        return Astro.ARTICLE.walkDirectory("*/*").map(dir -> findBy(dir.name()));
    }

    public static Signal<OpenThread> findLast(int size) {
        return findAll().scan(() -> new ArrayDeque(), (queue, thread) -> {
            if (queue.size() == size) {
                queue.pollFirst();
            }
            queue.addLast(thread);
            return queue;
        }).last().flatIterable(x -> x);
    }

    private static final void trail() {
        trail(Astro.ARTICLE.walkFile("**/thread.json").last().map(file -> findBy(file.parent().name())).to().exact());
    }

    private static final void trail(OpenThread current) {
        Server server = new Server();

        if (current.comments.size() < 985) {
            try {
                OpenThread thread = new OpenThread();
                thread.parse(server.fetchSource(current.url));

                Thread.sleep(1000);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        while (985 <= current.comments.size()) {
            try {
                OpenThread thread = new OpenThread();
                thread.parse(server.fetchSource(current.searchNextURL().v));

                Thread.sleep(1000);

                current = thread;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
        server.shutdown();
    }

    static String computeThreadNumber(String text) {
        Matcher matcher = Pattern.compile("\\d+").matcher(text);
        String last = null;
        while (matcher.find()) {
            last = matcher.group();
        }
        return last;
    }

    public static void main(String[] args) {
        Server server = new Server();
        OpenThread thread = new OpenThread();
        thread.parse(server.fetchSource("https://uni.open2ch.net/test/read.cgi/gameswf/1730457064/"));
        server.shutdown();

        trail(thread);
    }
}
