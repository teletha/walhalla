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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Tweet {

    public String id;

    public String author;

    public String text;

    public ZonedDateTime date;

    public List<String> media = new ArrayList();
}
