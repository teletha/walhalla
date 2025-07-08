/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.data;

public enum Rarity {

    王子, 鉄, 銅, 銀, 金, 白, 青, 黒;

    public boolean isRare() {
        return this != 鉄 && this != 銅 && this != 銀 && this != 王子;
    }
}