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

import java.util.List;

public class ImageSource {

    public String origin;

    public List<String> large;

    public List<String> huge;

    public List<String> backup;

    public boolean hasBackup() {
        return backup != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ImageSource [origin=" + origin + ", backupLarge=" + large + ", backupHuge=" + huge + "]";
    }
}
