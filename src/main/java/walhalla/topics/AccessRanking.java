/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.topics;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import kiss.I;
import kiss.JSON;
import walhalla.Astro;
import walhalla.topics.AccessRanking.Rank;

@SuppressWarnings("serial")
public class AccessRanking extends ArrayList<Rank> {

    record Rank(String article, int pv) {
    }

    public static void build() {
        AccessRanking ranking = new AccessRanking();

        String endpoint = "https://api.cloudflare.com/client/v4/graphql";
        String token = I.env("CloudflareAPIToken");
        String zoneId = I.env("CloudflareZoneID");

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String query = """
                {
                  viewer {
                    accounts(filter: {accountTag: "%s"}) {
                      totals: rumPageloadEventsAdaptiveGroups(limit: 30, filter: { date_geq: "%s", date_lt: "%s" }, orderBy: [count_DESC]) {
                        count
                        dimensions {
                          requestPath
                        }
                      }
                    }
                  }
                }
                """.formatted(zoneId, start.format(format), end.format(format));

        String body = "{\"query\":\"" + query.replace("\"", "\\\"").replace("\n", " ") + "\"}";

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        I.http(request, JSON.class).waitForTerminate().to(json -> {
            for (JSON item : json.find("data", "viewer", "accounts", "0", "totals", "*")) {
                try {
                    String path = URLDecoder.decode(item.get("dimensions").text("requestPath"), "UTF-8");
                    int count = item.get(int.class, "count");

                    if (path.startsWith("/posts/")) {
                        ranking.add(new Rank(path.substring(7, path.length() - 1), count));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw I.quiet(e);
                }
            }
        });

        I.write(ranking, Astro.PUBLIC.file("access-ranking.json").newBufferedWriter());
        I.info("Build access ranking.");
    }
}
