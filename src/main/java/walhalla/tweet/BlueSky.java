/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.tweet;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kiss.I;
import kiss.JSON;
import kiss.Signal;
import walhalla.image.Imgur;

public class BlueSky {

    private String APIKey = I.env("BlueSkyAPIKey");

    private String APISecret = I.env("BlueSkyAPISecret");

    private String XGDAPIKey = I.env("XGDAPIKey");

    public Signal<JSON> tweet(String title, String description, String url, String image, String hash) {
        return post("https://bsky.social/xrpc/com.atproto.server.createSession", Map.of(), """
                {
                    "identifier": "%s",
                    "password": "%s"
                }
                """, APIKey, APISecret).flatMap(json -> {
            String accessToken = "Bearer " + json.text("accessJwt");
            String did = json.text("did");

            // ハッシュタグのfacetsを生成
            String facetsJson = generateFacets(hash);

            // ソーシャルカード埋め込みを生成
            String embedJson = generateEmbed(title, description, url, image, accessToken);

            return post("https://bsky.social/xrpc/com.atproto.repo.createRecord", Map.of("Authorization", accessToken), """
                    {
                        "repo": "%s",
                        "collection": "app.bsky.feed.post",
                        "record": {
                            "text": "%s",
                            "createdAt": "%s",
                            "$type": "app.bsky.feed.post"%s%s
                        }
                    }
                    """, did, hash, Instant.now(), facetsJson, embedJson);
        });
    }

    /**
     * ソーシャルカード埋め込みのJSONを生成
     */
    private String generateEmbed(String title, String description, String url, String image, String accessToken) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        // 画像がある場合はblobをアップロード
        String thumbJson = "";
        if (image != null && !image.isEmpty()) {
            thumbJson = ", \"thumb\": " + uploadImage(image, accessToken);
        }

        return """
                ,
                "embed": {
                    "$type": "app.bsky.embed.external",
                    "external": {
                        "uri": "%s",
                        "title": "%s",
                        "description": "%s" %s
                    }
                }""".formatted(url, escapeJson(title), escapeJson(description), thumbJson);
    }

    /**
     * 画像をアップロードしてblobリファレンスを取得
     */
    private String uploadImage(String imagePath, String accessToken) {
        try {
            System.out.println(imagePath);
            // 画像ファイルを読み込み
            byte[] imageBytes = Imgur.download(imagePath).large().readAllBytes();

            // MIMEタイプを判定
            String mimeType = determineMimeType(imagePath);

            // 画像をアップロード
            HttpRequest.Builder uploadReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://bsky.social/xrpc/com.atproto.repo.uploadBlob"))
                    .header("Content-Type", mimeType)
                    .header("Authorization", accessToken)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes));
            JSON uploadResult = I.http(uploadReq, JSON.class).waitForTerminate().to().exact();

            // blobリファレンスを取得
            JSON blob = uploadResult.get("blob");

            return """
                    {
                        "$type": "blob",
                        "ref": {
                            "$link": "%s"
                        },
                        "mimeType": "%s",
                        "size": %d
                    }""".formatted(blob.get("ref").text("$link"), blob.text("mimeType"), Integer.parseInt(blob.text("size")));
        } catch (Exception e) {
            System.err.println("画像のアップロードに失敗しました: " + e.getMessage());
            throw I.quiet(e);
        }
    }

    private String determineMimeType(String imagePath) {
        String lowerPath = imagePath.toLowerCase();
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "image/jpeg"; // デフォルト
        }
    }

    /**
     * JSON文字列内の特殊文字をエスケープ
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\\\"").replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /**
     * メッセージからハッシュタグとURLを検出してfacetsのJSONを生成
     */
    private String generateFacets(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        java.util.List<String> facets = new java.util.ArrayList<>();

        // UTF-8バイト配列に変換
        byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String utf8Message = new String(messageBytes, java.nio.charset.StandardCharsets.UTF_8);

        // ハッシュタグを検出
        Pattern hashtagPattern = Pattern.compile("#([\\p{L}\\p{N}_]+)");
        Matcher hashtagMatcher = hashtagPattern.matcher(utf8Message);

        while (hashtagMatcher.find()) {
            int charStart = hashtagMatcher.start();
            int charEnd = hashtagMatcher.end();
            String tag = hashtagMatcher.group(1);

            int byteStart = utf8Message.substring(0, charStart).getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            int byteEnd = utf8Message.substring(0, charEnd).getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

            String facet = """
                    {
                        "index": {
                            "byteStart": %d,
                            "byteEnd": %d
                        },
                        "features": [{
                            "$type": "app.bsky.richtext.facet#tag",
                            "tag": "%s"
                        }]
                    }""".formatted(byteStart, byteEnd, tag);

            facets.add(facet);
        }

        if (facets.isEmpty()) {
            return "";
        }

        return ",\"facets\": [" + String.join(",", facets) + "]";
    }

    private Signal<JSON> post(String uri, Map<String, String> header, String body, Object... variables) {
        Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.formatted(variables)));

        header.forEach(builder::header);

        return I.http(builder, JSON.class);
    }
}