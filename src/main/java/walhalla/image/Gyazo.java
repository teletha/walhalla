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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayList;

import kiss.I;
import kiss.JSON;
import psychopath.File;

public class Gyazo {

    public static JSON meta(String imageURL) {
        return I.http("https://api.gyazo.com/api/oembed?url=" + imageURL, JSON.class).waitForTerminate().to().exact();
    }

    public static JSON upload(File image) {
        return upload(image.name(), image.newInputStream());
    }

    public static JSON upload(String name, InputStream data) {
        String token = I.env("GyazoAPIToken");

        // 境界線定義
        String boundary = "----GyazoBoundary" + System.currentTimeMillis();

        // multipart/form-dataのボディ構築
        BodyPublisher body = buildMultipartBody(token, name, data, boundary);

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("https://upload.gyazo.com/api/upload"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(body);

        I.info("Uploading image to Gyazo: " + name);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
        return I.http(request, JSON.class).waitForTerminate().to().exact();
    }

    private static BodyPublisher buildMultipartBody(String token, String name, InputStream data, String boundary) {
        try {
            var byteArrays = new ArrayList<byte[]>();

            // access_tokenパート
            byteArrays.add(("--" + boundary + "\r\n").getBytes());
            byteArrays.add("Content-Disposition: form-data; name=\"access_token\"\r\n\r\n".getBytes());
            byteArrays.add((token + "\r\n").getBytes());

            // imagedataパート
            byteArrays.add(("--" + boundary + "\r\n").getBytes());
            byteArrays.add(("Content-Disposition: form-data; name=\"imagedata\"; filename=\"" + name + "\"\r\n").getBytes());
            byteArrays.add(("Content-Type: image/png\r\n\r\n").getBytes());
            byteArrays.add(data.readAllBytes());
            byteArrays.add("\r\n".getBytes());

            // 終端
            byteArrays.add(("--" + boundary + "--\r\n").getBytes());

            return BodyPublishers.ofByteArrays(byteArrays);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
