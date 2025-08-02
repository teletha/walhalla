/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.twitter;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import kiss.I;

public class Twitter {

    public static void post(String tweet) {
        try {
            String oauthAPIKey = I.env("TwitterAPIKey");
            String oauthAPISecret = I.env("TwitterAPISecret");
            String oauthNonce = UUID.randomUUID().toString().replaceAll("-", "");
            String oauthTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String oauthToken = I.env("TwitterAccessToken");
            String oauthTokenSecret = I.env("TwitterAccessTokenSecret");
            String oauthVersion = "1.0";

            String paramsString = String
                    .join("&", "oauth_consumer_key=" + encode(oauthAPIKey), "oauth_nonce=" + encode(oauthNonce), "oauth_signature_method=" + encode("HMAC-SHA1"), "oauth_timestamp=" + encode(oauthTimestamp), "oauth_token=" + encode(oauthToken), "oauth_version=" + encode(oauthVersion));

            String endpoint = "https://api.twitter.com/2/tweets";
            String baseString = "POST&" + encode(endpoint) + "&" + encode(paramsString);
            String signingKey = encode(oauthAPISecret) + "&" + encode(oauthTokenSecret);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(signingKey.getBytes("UTF-8"), "HmacSHA1"));
            String signature = encode(Base64.encodeBase64String(mac.doFinal(baseString.getBytes("UTF-8"))));

            String oauthHeader = String
                    .join(", ", "oauth_consumer_key=\"" + oauthAPIKey + "\"", "oauth_nonce=\"" + oauthNonce + "\"", "oauth_signature=\"" + signature + "\"", "oauth_signature_method=\"HMAC-SHA1\"", "oauth_timestamp=\"" + oauthTimestamp + "\"", "oauth_token=\"" + oauthToken + "\"", "oauth_version=\"1.0\"");

            HttpRequest.Builder request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "OAuth " + oauthHeader)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + tweet + "\"}"));

            I.http(request, String.class).waitForTerminate().to(body -> {
                System.out.println(body);
            });
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw I.quiet(e);
        }
    }
}