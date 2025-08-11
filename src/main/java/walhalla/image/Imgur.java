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

public class Imgur {

    public static Image download(String url) {
        if (url.startsWith("https://imgur.com/a/")) {
            int index = url.indexOf("#");
            url = "https://i.imgur.com/" + url.substring(index + 1) + ".jpg";
        }

        if (!url.startsWith("https://i.imgur.com/")) {
            throw new IllegalArgumentException("Invalid Imgur URL: " + url);
        }

        int index = url.lastIndexOf('.');
        String ext = url.substring(index + 1);
        url = url.substring(0, index);
        String name = url.substring(20);

        if (ext.equals("gif")) {
            return new Image(name, downloadImage(url + ".gif"), name + "-l", downloadImage(url + "l.webp"));
        } else {
            return new Image(name + "-h", downloadImage(url + "h.webp"), name + "-l", downloadImage(url + "l.webp"));
        }
    }

    public static InputStream downloadImage(String image) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(image))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Referer", "https://imgur.com/");

        I.info("Downloading image from Imgur: " + image);
        return I.http(request, InputStream.class).waitForTerminate().to().exact();
    }
}
