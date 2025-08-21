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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class Twitter {

    private String APIKey = I.env("TwitterAPIKey");

    private String APISecret = I.env("TwitterAPISecret");

    private String accessToken = I.env("TwitterAccessToken");

    private String accessTokenSecret = I.env("TwitterAccessTokenSecret");

    private String bearerToken = I.env("TwitterBearerToken");

    public Twitter() {
    }

    public Twitter setAPIKey(String key, String secret) {
        this.APIKey = key;
        this.APISecret = secret;
        return this;
    }

    public Twitter setAccessToken(String token, String secret) {
        this.accessToken = token;
        this.accessTokenSecret = secret;
        return this;
    }

    public void fetch() {
        try {
            get("https://api.twitter.com/2/tweets/1958119280467476537", "").waitForTerminate().to(json -> {
                System.out.println(json);
            });
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) {
        new Twitter().fetch();
    }

    public Signal<JSON> tweet(String title, String description, String url, String image, String hash) {
        if (description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }

        try {
            return post("https://api.twitter.com/2/tweets", "{\"text\":\"" + description + "\\n" + hash + "\\n" + url + "\"}");
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    private Signal<JSON> get(String endpoint, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Accept", "*/*")
                .GET();

        return I.http(request, JSON.class);
    }

    private Signal<JSON> post(String endpoint, String body) throws Exception {
        String oauthNonce = UUID.randomUUID().toString().replaceAll("-", "");
        String oauthTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String oauthVersion = "1.0";

        String paramsString = String
                .join("&", "oauth_consumer_key=" + encode(APIKey), "oauth_nonce=" + encode(oauthNonce), "oauth_signature_method=" + encode("HMAC-SHA1"), "oauth_timestamp=" + encode(oauthTimestamp), "oauth_token=" + encode(accessToken), "oauth_version=" + encode(oauthVersion));

        String baseString = "POST&" + encode(endpoint) + "&" + encode(paramsString);
        String signingKey = encode(APISecret) + "&" + encode(accessTokenSecret);

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
        String signature = encode(Base64.getEncoder().encodeToString(rawHmac));

        String oauthHeader = String
                .join(", ", "oauth_consumer_key=\"" + APIKey + "\"", "oauth_nonce=\"" + oauthNonce + "\"", "oauth_signature=\"" + signature + "\"", "oauth_signature_method=\"HMAC-SHA1\"", "oauth_timestamp=\"" + oauthTimestamp + "\"", "oauth_token=\"" + accessToken + "\"", "oauth_version=\"1.0\"");

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "OAuth " + oauthHeader)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        return I.http(request, JSON.class);
    }

    private String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
}