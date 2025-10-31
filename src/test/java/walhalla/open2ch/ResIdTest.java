/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.open2ch;

import org.junit.jupiter.api.Test;

public class ResIdTest {

    @Test
    void integer() {
        ResId id = ResId.of(10);
        assert id.id == 10;
        assert id.thread == 0;
        assert id.emphasized == false;

        id = ResId.of(-10);
        assert id.id == 10;
        assert id.thread == 0;
        assert id.emphasized == true;
    }

    @Test
    void decimal() {
        ResId id = ResId.of(10.004);
        assert id.id == 4;
        assert id.thread == 10;
        assert id.emphasized == false;

        id = ResId.of(-10.004);
        assert id.id == 4;
        assert id.thread == 10;
        assert id.emphasized == true;

        id = ResId.of(-10.994);
        assert id.id == 994;
        assert id.thread == 10;
        assert id.emphasized == true;

        id = ResId.of(-10.8);
        assert id.id == 800;
        assert id.thread == 10;
        assert id.emphasized == true;
    }
}
