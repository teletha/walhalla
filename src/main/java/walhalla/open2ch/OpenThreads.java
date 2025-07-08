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

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import kiss.I;
import kiss.Signal;
import kiss.WiseConsumer;
import kiss.XML;
import walhalla.Astro;

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
public class OpenThreads {

    /** Ensures that threads are only updated once per session. */
    private static boolean initialized;

    /**
     * Returns a signal (reactive stream) of all parsed thread data.
     * If this is the first call, it triggers the update process.
     * 
     * @return a Signal containing {@link OpenThread} objects
     */
    public static synchronized Signal<OpenThread> findAll() {
        if (initialized == false) {
            initialized = true;
            crawl();
        }

        return Astro.ARTICLE.walkDirectory("*").map(OpenThread::new);
    }

    /**
     * Update thread data by crawling recent thread entries and retrieving
     * updated HTML source if needed.
     * 
     * <p>
     * This method is automatically invoked once before the first call to {@link #findAll()}.
     */
    private static void crawl() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Server server = new Server();

        I.http("https://find.open2ch.net/?q=アイギス", XML.class)
                .waitForTerminate()
                .flatIterable(html -> html.find("div.subject a"))
                .to((WiseConsumer<XML>) link -> {
                    String uri = link.attr("href").substring(0, link.attr("href").length() - 3);
                    int id = Integer.parseInt(uri.substring(uri.lastIndexOf("/", uri.length() - 2) + 1, uri.length() - 1));
                    String title = link.text();
                    int end = title.lastIndexOf(')');
                    int start = title.lastIndexOf('(', end);
                    int size = Integer.parseInt(title.substring(start + 1, end));
                    title = title.substring(0, start).trim();
                    String num = computeThreadNumber(title);
                    LocalDateTime date = LocalDateTime.parse(link.parent().parent().next().firstChild().lastChild().text(), formatter);

                    OpenThread thread = new OpenThread(num, id);
                    if (975 <= size && thread.parsedJSON.lastModifiedDateTime().toLocalDateTime().plusHours(1).isBefore(date)) {
                        server.pending = new CompletableFuture();
                        Desktop.getDesktop().browse(new URI(uri + "#audit"));
                        thread.parse(server.pending.get(30, TimeUnit.SECONDS));

                        Thread.sleep(1000);
                    }
                });

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

    /**
     * A minimal local HTTP server used to receive HTML thread data from the browser.
     * 
     * <p>
     * When a thread needs to be updated, the thread page is opened in the default browser
     * and the browser is expected to send back the HTML source via a POST request.
     */
    private static class Server {

        /** The embedded HTTP server listening for POSTed thread HTML. */
        private final HttpServer server;

        /** The future that will be completed with the HTML content. */
        private CompletableFuture<String> pending;

        /**
         * Create and start the local HTTP server on port 13849.
         */
        private Server() {
            try {
                server = HttpServer.create(new InetSocketAddress(13849), 0);
                server.createContext("/", this::handle);
                server.setExecutor(I.Jobs);
                server.start();
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        /**
         * Handle incoming HTTP requests. Supports:
         * <ul>
         * <li>OPTIONS preflight for CORS</li>
         * <li>POST requests to receive HTML data</li>
         * </ul>
         * 
         * @param exchange the HTTP exchange object
         * @throws IOException if an I/O error occurs
         */
        private void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1); // No content
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                String response = "OK";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

                synchronized (this) {
                    if (pending != null) {
                        pending.complete(body);
                        pending = null;
                    }
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        /**
         * Stop the server.
         */
        private void shutdown() {
            server.stop(0);
        }
    }
}
