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

public class CommentIdTest {

    @Test
    void integer() {
        CommentId id = CommentId.of(10);
        assert id.id == 10;
        assert id.thread == 0;
        assert id.emphasized == false;
        assert id.toString().equals("10");

        id = CommentId.of(-10);
        assert id.id == 10;
        assert id.thread == 0;
        assert id.emphasized == true;
        assert id.toString().equals("-10");

        id = CommentId.of(1010);
        assert id.id == 10;
        assert id.thread == 1;
        assert id.emphasized == false;
        assert id.toString().equals("1010");

        id = CommentId.of(-33980);
        assert id.id == 980;
        assert id.thread == 33;
        assert id.emphasized == true;
        assert id.toString().equals("-33980");
    }

    @Test
    void decimal() {
        CommentId id = CommentId.of(1234.004);
        assert id.id == 4;
        assert id.thread == 1234;
        assert id.emphasized == false;
        assert id.toString().equals("1234.004");

        id = CommentId.of(-2353.004);
        assert id.id == 4;
        assert id.thread == 2353;
        assert id.emphasized == true;
        assert id.toString().equals("-2353.004");

        id = CommentId.of(-98765.994);
        assert id.id == 994;
        assert id.thread == 98765;
        assert id.emphasized == true;
        assert id.toString().equals("-98765.994");

        id = CommentId.of(-467587.800);
        assert id.id == 800;
        assert id.thread == 467587;
        assert id.emphasized == true;
        assert id.toString().equals("-467587.800");
    }
}
