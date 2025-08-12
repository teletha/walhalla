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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single response (res) in an open2ch thread.
 * A response includes metadata such as poster name, posting time,
 * identifier, message body, and optionally attached media.
 */
public class Res {

    /**
     * The sequential number of this response in the thread.
     * Starts from 1 for the first post.
     */
    public int num;

    /**
     * The name of the poster.
     * This may be a username, a default name, or a tripcode.
     */
    public String name;

    /**
     * The timestamp indicating when this response was posted.
     */
    public LocalDateTime date;

    /**
     * The unique ID of the poster, as assigned by the forum system.
     * This is often used for tracking posts by the same user.
     */
    public String id;

    /**
     * The main body text of the response.
     */
    public String body;

    /**
     * A list of image URLs attached to this response.
     * These are usually external links to image hosting services.
     */
    public List<ImageSource> sources = new ArrayList();

    /**
     * A list of embedded media links (e.g., YouTube, X/Twitter) in the response.
     * These are typically identified and parsed separately from the text body.
     */
    public List<String> embeds;

    public List<Integer> from;

    public List<Integer> to;

    /**
     * The thread to which this response belongs.
     */
    OpenThread thread;
}
