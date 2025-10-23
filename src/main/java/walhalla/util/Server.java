/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.util;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import kiss.I;

/**
 * A minimal local HTTP server used to receive HTML thread data from the browser.
 * 
 * <p>
 * When a thread needs to be updated, the thread page is opened in the default browser
 * and the browser is expected to send back the HTML source via a POST request.
 */
public class Server {

    /** The embedded HTTP server listening for POSTed thread HTML. */
    private final HttpServer server;

    /** The future that will be completed with the HTML content. */
    private CompletableFuture<String> pending;

    /**
     * Create and start the local HTTP server on port 13849.
     */
    public Server() {
        try {
            server = HttpServer.create(new InetSocketAddress(13849), 0);
            server.createContext("/", this::handle);
            server.setExecutor(I.Jobs);
            server.start();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public String fetchSource(String uri) {
        try {
            pending = new CompletableFuture();
            Desktop.getDesktop().browse(new URI(uri + "#audit"));
            return pending.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new Error(e);
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
    public void shutdown() {
        server.stop(0);
    }
}