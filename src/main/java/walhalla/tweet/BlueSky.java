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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kiss.I;
import kiss.JSON;
import kiss.Signal;
import walhalla.image.Imgur;

/**
 * BlueSky（AT Protocol）への投稿機能を提供するクラス。
 * 
 * <p>このクラスは以下の機能を提供します：</p>
 * <ul>
 *   <li>テキスト投稿とハッシュタグの自動検出</li>
 *   <li>ソーシャルカードの埋め込み</li>
 *   <li>画像のアップロードとサムネイル設定</li>
 *   <li>日本語を含むマルチバイト文字の正確な位置計算</li>
 * </ul>
 * 
 * <p>環境変数の設定が必要です：</p>
 * <ul>
 *   <li>{@code BlueSkyAPIKey}: BlueSkyのユーザー名またはメールアドレス</li>
 *   <li>{@code BlueSkyAPISecret}: BlueSkyのアプリパスワード</li>
 *   <li>{@code XGDAPIKey}: X.gd短縮URLサービスのAPIキー（オプション）</li>
 * </ul>
 * 
 * @author Nameless Production Committee
 * @since 1.0
 */
public class BlueSky {

    /** BlueSkyのユーザー識別子（ユーザー名またはメールアドレス） */
    private final String apiKey = I.env("BlueSkyAPIKey");

    /** BlueSkyのアプリパスワード */
    private final String apiSecret = I.env("BlueSkyAPISecret");

    /** X.gd短縮URLサービスのAPIキー */
    private final String xgdApiKey = I.env("XGDAPIKey");

    /** ハッシュタグを検出する正規表現パターン */
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");

    /**
     * BlueSkyに投稿を行います。
     * 
     * @param title ソーシャルカードのタイトル
     * @param description ソーシャルカードの説明文
     * @param url 埋め込むURL
     * @param image サムネイル画像のパス（空文字の場合は画像なし）
     * @param hash 投稿テキスト（ハッシュタグが自動検出されます）
     * @return 投稿結果のJSON
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    public Signal<JSON> tweet(String title, String description, String url, String image, String hash) {
        validateParameters(title, description, url, hash);
        
        return createSession()
                .flatMap(session -> createPost(session, title, description, url, image, hash));
    }

    /**
     * パラメータの妥当性を検証します。
     * 
     * @param title タイトル
     * @param description 説明文
     * @param url URL
     * @param hash ハッシュタグを含む投稿テキスト
     * @throws IllegalArgumentException パラメータが不正な場合
     */
    private void validateParameters(String title, String description, String url, String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            throw new IllegalArgumentException("投稿テキスト（hash）は必須です");
        }
        if (apiKey == null || apiSecret == null) {
            throw new IllegalStateException("BlueSkyAPIKeyとBlueSkyAPISecretの環境変数設定が必要です");
        }
    }

    /**
     * BlueSkyセッションを作成します。
     * 
     * @return セッション情報を含むJSON
     */
    private Signal<JSON> createSession() {
        return post("https://bsky.social/xrpc/com.atproto.server.createSession", Map.of(), """
                {
                    "identifier": "%s",
                    "password": "%s"
                }
                """, apiKey, apiSecret);
    }

    /**
     * 実際の投稿を作成します。
     * 
     * @param session セッション情報
     * @param title タイトル
     * @param description 説明文
     * @param url URL
     * @param image 画像パス
     * @param hash 投稿テキスト
     * @return 投稿結果のJSON
     */
    private Signal<JSON> createPost(JSON session, String title, String description, String url, String image, String hash) {
        String accessToken = "Bearer " + session.text("accessJwt");
        String did = session.text("did");

        String facetsJson = generateFacets(hash);
        String embedJson = generateEmbed(title, description, url, image, accessToken);

        return post("https://bsky.social/xrpc/com.atproto.repo.createRecord", 
                   Map.of("Authorization", accessToken), """
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
    }

    /**
     * ソーシャルカード埋め込みのJSONを生成します。
     * 
     * @param title カードのタイトル
     * @param description カードの説明文
     * @param url 埋め込むURL
     * @param image サムネイル画像のパス（空文字の場合は画像なし）
     * @param accessToken BlueSkyのアクセストークン
     * @return 埋め込み用JSON文字列
     */
    private String generateEmbed(String title, String description, String url, String image, String accessToken) {
        if (isEmptyString(url)) {
            return "";
        }

        String thumbJson = generateThumbnailJson(image, accessToken);

        return """
                ,
                "embed": {
                    "$type": "app.bsky.embed.external",
                    "external": {
                        "uri": "%s",
                        "title": "%s",
                        "description": "%s"%s
                    }
                }""".formatted(url, escapeJson(title), escapeJson(description), thumbJson);
    }

    /**
     * サムネイル画像のJSONを生成します。
     * 
     * @param image 画像パス
     * @param accessToken アクセストークン
     * @return サムネイル用JSON文字列
     */
    private String generateThumbnailJson(String image, String accessToken) {
        if (isEmptyString(image)) {
            return "";
        }

        try {
            String blobJson = uploadImage(image, accessToken);
            return ", \"thumb\": " + blobJson;
        } catch (Exception e) {
            I.warn("サムネイル画像のアップロードに失敗しました: " + image, e);
            return "";
        }
    }

    /**
     * 画像をBlueSkyにアップロードしてblobリファレンスを取得します。
     * 
     * @param imagePath 画像ファイルのパスまたはURL
     * @param accessToken BlueSkyのアクセストークン
     * @return blobリファレンスのJSON文字列
     * @throws Exception アップロードに失敗した場合
     */
    private String uploadImage(String imagePath, String accessToken) throws Exception {
        byte[] imageBytes = downloadImageBytes(imagePath);
        String mimeType = determineMimeType(imagePath);

        HttpRequest uploadRequest = createUploadRequest(imageBytes, mimeType, accessToken);
        JSON uploadResult = I.http(uploadRequest, JSON.class).waitForTerminate().to().exact();

        return formatBlobReference(uploadResult);
    }

    /**
     * 画像データをダウンロードします。
     * 
     * @param imagePath 画像パスまたはURL
     * @return 画像のバイト配列
     * @throws Exception ダウンロードに失敗した場合
     */
    private byte[] downloadImageBytes(String imagePath) throws Exception {
        return Imgur.download(imagePath).large().readAllBytes();
    }

    /**
     * 画像アップロード用のHTTPリクエストを作成します。
     * 
     * @param imageBytes 画像データ
     * @param mimeType MIMEタイプ
     * @param accessToken アクセストークン
     * @return HTTPリクエスト
     */
    private HttpRequest createUploadRequest(byte[] imageBytes, String mimeType, String accessToken) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://bsky.social/xrpc/com.atproto.repo.uploadBlob"))
                .header("Content-Type", mimeType)
                .header("Authorization", accessToken)
                .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                .build();
    }

    /**
     * アップロード結果からblobリファレンスをフォーマットします。
     * 
     * @param uploadResult アップロード結果のJSON
     * @return フォーマットされたblobリファレンス
     */
    private String formatBlobReference(JSON uploadResult) {
        JSON blob = uploadResult.get("blob");
        return """
                {
                    "$type": "blob",
                    "ref": {
                        "$link": "%s"
                    },
                    "mimeType": "%s",
                    "size": %d
                }""".formatted(
                blob.get("ref").text("$link"),
                blob.text("mimeType"),
                Integer.parseInt(blob.text("size")));
    }

    /**
     * ファイル拡張子からMIMEタイプを判定します。
     * 
     * @param imagePath 画像ファイルのパス
     * @return 対応するMIMEタイプ
     */
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
     * JSON文字列内の特殊文字をエスケープします。
     * 
     * @param text エスケープするテキスト
     * @return エスケープされたテキスト
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\\\"")
                  .replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * メッセージからハッシュタグを検出してfacetsのJSONを生成します。
     * 
     * <p>日本語を含むマルチバイト文字でも正確なバイト位置を計算します。</p>
     * 
     * @param message ハッシュタグを含むメッセージ
     * @return facets用JSON文字列
     */
    private String generateFacets(String message) {
        if (isEmptyString(message)) {
            return "";
        }

        List<String> facets = extractHashtagFacets(message);
        
        if (facets.isEmpty()) {
            return "";
        }

        return ",\"facets\": [" + String.join(",", facets) + "]";
    }

    /**
     * メッセージからハッシュタグのfacetリストを抽出します。
     * 
     * @param message 検索対象のメッセージ
     * @return facetのリスト
     */
    private List<String> extractHashtagFacets(String message) {
        List<String> facets = new ArrayList<>();
        String utf8Message = normalizeToUtf8(message);
        Matcher matcher = HASHTAG_PATTERN.matcher(utf8Message);

        while (matcher.find()) {
            String facet = createHashtagFacet(utf8Message, matcher);
            facets.add(facet);
        }

        return facets;
    }

    /**
     * 文字列をUTF-8で正規化します。
     * 
     * @param message 正規化する文字列
     * @return UTF-8で正規化された文字列
     */
    private String normalizeToUtf8(String message) {
        byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new String(messageBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * ハッシュタグのfacetを作成します。
     * 
     * @param message メッセージ全体
     * @param matcher マッチャー
     * @return facetのJSON文字列
     */
    private String createHashtagFacet(String message, Matcher matcher) {
        int charStart = matcher.start();
        int charEnd = matcher.end();
        String tag = matcher.group(1);

        int byteStart = calculateBytePosition(message, charStart);
        int byteEnd = calculateBytePosition(message, charEnd);

        return """
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
    }

    /**
     * 文字位置からUTF-8バイト位置を計算します。
     * 
     * @param message メッセージ
     * @param charPosition 文字位置
     * @return バイト位置
     */
    private int calculateBytePosition(String message, int charPosition) {
        return message.substring(0, charPosition)
                     .getBytes(java.nio.charset.StandardCharsets.UTF_8)
                     .length;
    }

    /**
     * 文字列が空またはnullかどうかを判定します。
     * 
     * @param str 判定する文字列
     * @return 空またはnullの場合true
     */
    private boolean isEmptyString(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * HTTPリクエストを送信してJSONレスポンスを取得します。
     * 
     * @param uri リクエストURI
     * @param headers 追加ヘッダー
     * @param body リクエストボディ
     * @param variables ボディのフォーマット変数
     * @return レスポンスのJSON
     */
    private Signal<JSON> post(String uri, Map<String, String> headers, String body, Object... variables) {
        Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.formatted(variables)));

        headers.forEach(builder::header);

        return I.http(builder, JSON.class);
    }
}