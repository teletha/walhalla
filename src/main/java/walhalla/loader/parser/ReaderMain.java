/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.loader.parser;

import psychopath.File;
import psychopath.Locator;
import walhalla.loader.parser.data.AlObject;

public class ReaderMain {

    public static void main(String[] args) {
        File file = Locator.file("src/main/resources/SkillText.atb");
        AlObject root = new AlParser().parse(file.bytes());
        System.out.println(root);
    }
}
