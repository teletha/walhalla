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

public class Author {

    public String id;

    public String name;

    public String userName;

    public String icon;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Author [id=" + id + ", name=" + name + ", userName=" + userName + ", icon=" + icon + "]";
    }
}
