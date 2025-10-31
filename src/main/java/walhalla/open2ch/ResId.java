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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ResId {

    public final boolean emphasized;

    public final int thread;

    public final int id;

    private ResId(int thread, int id, boolean emphasized) {
        this.thread = thread;
        this.id = id;
        this.emphasized = emphasized;
    }

    public static ResId of(int value) {
        return new ResId(0, Math.abs(value), value < 0);
    }

    public static ResId of(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value);
        BigDecimal thread = decimal.setScale(0, RoundingMode.DOWN);
        BigDecimal id = decimal.subtract(thread).multiply(BigDecimal.valueOf(1000));

        return new ResId(thread.abs().intValue(), id.abs().intValue(), thread.signum() < 0);
    }
}
