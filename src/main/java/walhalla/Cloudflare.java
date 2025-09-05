/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import kiss.I;
import kiss.JSON;

public class Cloudflare {

    public static void main(String[] args) {
        String endpoint = "https://api.cloudflare.com/client/v4/graphql";
        String token = I.env("CloudflareAPIToken");
        String zoneId = "b0b2dff6147a4a65205903a8b0501caf";

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date_geq = startDate.format(fmt);
        String date_lt = today.format(fmt);

        String graphqlQuery = """
                {
                  viewer {
                    accounts(filter: {accountTag: "%s"}) {
                      totals: rumPageloadEventsAdaptiveGroups(limit: 100, filter: { date_geq: "%s", date_lt: "%s" }, orderBy: [count_DESC]) {
                        count
                        sum {
                          visits
                        }
                        dimensions {
                          requestPath
                        }
                      }
                    }
                  }
                }
                                                """
                .formatted(zoneId, date_geq, date_lt);

        String body = "{\"query\":\"" + graphqlQuery.replace("\"", "\\\"").replace("\n", " ") + "\"}";

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        I.http(request, JSON.class).waitForTerminate().to(json -> {
            System.out.println(json);
        });
    }
}