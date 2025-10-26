/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.image;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;

import kiss.I;

public class General {

    public static Image download(String uri) {
        int index = uri.lastIndexOf("/");

        return new Image(uri.substring(index + 1), downloadImage(uri));
    }

    public static InputStream downloadImage(String image) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(image))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Referer", "https://imgur.com/");

        I.info("Downloading image from Open Imgr: " + image);
        return I.http(request, InputStream.class).waitForTerminate().to().exact();
    }
}
