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

import kiss.I;
import kiss.XML;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

/**
 * <p>
 * Wikiクラスは、サーバーから取得したデータをローカルディレクトリにキャッシュし、一定期間（デフォルト14日間）有効とするユーティリティクラスです。
 * キャッシュが存在し有効な場合はキャッシュを返し、期限切れや未取得の場合はサーバーからデータを取得してキャッシュします。
 * </p>
 * <ul>
 * <li>キャッシュディレクトリは「.data/cache」配下に作成されます。</li>
 * <li>キャッシュの有効期限（TTL）はデフォルト14日間ですが、Astro.FORCE_UPDATEに含まれる場合は即時更新されます。</li>
 * <li>サーバーへのリクエスト間隔（interval）はURLにより異なります。</li>
 * </ul>
 */
public class WebPage {

    /**
     * キャッシュデータを保存するディレクトリ。
     */
    private static final Directory CACHE_DIR = Locator.directory(".data/cache");

    /**
     * 最後にサーバーへリクエストした時刻（ミリ秒）。
     */
    private static long lastRequestTime = 0;

    /**
     * 指定したURIからデータを取得します。キャッシュが有効な場合はキャッシュを返し、
     * 期限切れや未取得の場合はサーバーから取得してキャッシュします。
     *
     * @param uri データ取得先のURI
     * @return データの文字列
     */
    public static String fetchText(String uri) {
        return fetchText(uri, 21 * 24 * 60 * 60 * 1000);
    }

    /**
     * 指定したURIからデータを取得します。キャッシュの有効期限（ttl）を指定できます。
     *
     * @param uri データ取得先のURI
     * @param ttl キャッシュの有効期限（ミリ秒）
     * @return データの文字列
     */
    public static String fetchText(String uri, long ttl) {
        return fetchText(uri, ttl, computeInteval(uri));
    }

    /**
     * 指定したURIからデータを取得します。キャッシュの有効期限（ttl）とリクエスト間隔（interval）を指定できます。
     *
     * @param uri データ取得先のURI
     * @param ttl キャッシュの有効期限（ミリ秒）
     * @param interval サーバーリクエスト間隔（ミリ秒）
     * @return データの文字列
     */
    public static String fetchText(String uri, long ttl, long interval) {
        String hash = String.valueOf(uri.hashCode());
        File file = CACHE_DIR.file(hash);

        if (file.isPresent()) {
            long lastModified = file.lastModifiedMilli();
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastModified < ttl) {
                return file.text();
            }
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < interval) {
            try {
                Thread.sleep(interval - (currentTime - lastRequestTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        uri = uri.replace(" ", "%20").replace("(", "%28").replace(")", "%29").replace("'", "%27");

        System.out.println("Downloading " + uri);
        String data = I.http(uri, String.class).waitForTerminate().to().exact();

        file.text(data);
        lastRequestTime = System.currentTimeMillis();

        return data;
    }

    /**
     * 指定したURIからXMLデータを取得します。キャッシュが有効な場合はキャッシュを返し、
     * 期限切れや未取得の場合はサーバーから取得してキャッシュします。
     *
     * @param uri データ取得先のURI
     * @return XMLデータ
     */
    public static XML fetchXML(String uri) {
        return fetchXML(uri, 21 * 24 * 60 * 60 * 1000);
    }

    /**
     * 指定したURIからXMLデータを取得します。キャッシュの有効期限（ttl）を指定できます。
     *
     * @param uri データ取得先のURI
     * @param ttl キャッシュの有効期限（ミリ秒）
     * @return XMLデータ
     */
    public static XML fetchXML(String uri, long ttl) {
        return fetchXML(uri, ttl, computeInteval(uri));
    }

    /**
     * 指定したURIからXMLデータを取得します。キャッシュの有効期限（ttl）とリクエスト間隔（interval）を指定できます。
     *
     * @param uri データ取得先のURI
     * @param ttl キャッシュの有効期限（ミリ秒）
     * @param interval サーバーリクエスト間隔（ミリ秒）
     * @return XMLデータ
     */
    public static XML fetchXML(String uri, long ttl, long interval) {
        return I.xml(fetchText(uri, ttl, interval));
    }

    /**
     * URIに応じてサーバーリクエスト間隔（ミリ秒）を計算します。
     *
     * @param uri データ取得先のURI
     * @return リクエスト間隔（ミリ秒）
     */
    private static long computeInteval(String uri) {
        return uri.startsWith("https://wikiwiki.jp/aigiszuki/") ? 1000 : 250;
    }
}