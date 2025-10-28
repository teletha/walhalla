/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.loader;

import java.io.IOException;
import java.util.List;

import psychopath.File;
import psychopath.Locator;

public class DecodeFileList {
    // Lua: decode_list(text) = decode(text, 0xea ~ 0x30)
    private static final int DECODE_KEY = 0xea ^ 0x30;

    public static void main(String[] args) throws IOException {
        List<String> excludePrefixes = List
                .of("Texture", "Skin", "Reward", "Stamp", "Shop", "Recommend", "Quest", "Promotion", "PrivateRoom", "PlayerDot", "Payment", "Overlay", "OnCommnad", "Message", "Map", "Sound", "Battle", "EnemyDot", "HarlemCG", "EvtCard");

        File file = Locator.file("src/main/resources/1fp32igvpoxnb521p9dqypak5cal0xv0");
        byte[] data = file.bytes();
        byte[] decoded = decode(data, DECODE_KEY, 0);
        String text = new String(decoded);
        root: for (String line : text.split("\n")) {
            String[] split = line.split(",");
            String url = "http://drc1bk94f7rq8.cloudfront.net/" + split[0] + "/" + split[1];
            String name = split[4];

            if (name.endsWith(".atb")) {
                for (String prefix : excludePrefixes) {
                    if (name.startsWith(prefix)) {
                        continue root;
                    }
                }

                // I.http(url, InputStream.class).waitForTerminate().to(input -> {
                // Locator.file(".data/raw/" + name).writeFrom(input);
                // });
                //
                // System.out.println(url + " " + name);
            } else {
                System.out.println(url + "   " + name);
            }
        }
    }

    // Java版 decode
    private static byte[] decode(byte[] text, int key, int offset) {
        byte[] result = new byte[text.length - offset];
        for (int i = offset; i < text.length; i++) {
            result[i - offset] = (byte) (text[i] ^ key);
        }
        return result;
    }
}