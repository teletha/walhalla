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

public class ImageSource {

    public String origin;

    public String backupL;

    public String backupH;

    public boolean hasBackup() {
        return backupL != null || backupH != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ImageSource [origin=" + origin + ", backupL=" + backupL + ", backupH=" + backupH + "]";
    }
}
