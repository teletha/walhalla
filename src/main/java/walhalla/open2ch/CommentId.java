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
import java.util.Objects;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import walhalla.Astro;

public class CommentId {

    static {
        I.load(Astro.class);
    }

    public final boolean emphasized;

    public final int thread;

    public final int id;

    public CommentId(int thread, int id, boolean emphasized) {
        this.thread = thread;
        this.id = id;
        this.emphasized = emphasized;
    }

    public boolean isExternalThreadIndex() {
        return 0 < thread && thread < 1000;
    }

    public boolean isExternalThreadId() {
        return 1000 < thread;
    }

    public boolean isInternalThread() {
        return thread == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, thread);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CommentId other = (CommentId) obj;
        return id == other.id && thread == other.thread;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (emphasized) builder.append("-");
        if (thread == 0) {
            builder.append(id);
        } else if (thread < 1000) {
            builder.append(thread * 1000 + id);
        } else {
            builder.append(thread).append(".");
            if (id < 10) {
                builder.append("00").append(id);
            } else if (id < 100) {
                builder.append("0").append(id);
            } else {
                builder.append(id);
            }
        }
        return builder.toString();
    }

    public static CommentId of(int value) {
        boolean emphasized = value < 0;
        value = Math.abs(value);

        if (value <= 1000) {
            return new CommentId(0, value, emphasized);
        } else {
            int id = value % 1000;
            int thread = (value - id) / 1000;
            return new CommentId(thread, id, emphasized);
        }
    }

    public static CommentId of(double value) {
        return of(BigDecimal.valueOf(value));
    }

    public static CommentId of(BigDecimal decimal) {
        BigDecimal thread = decimal.setScale(0, RoundingMode.DOWN);
        BigDecimal id = decimal.subtract(thread).multiply(BigDecimal.valueOf(1000));

        if (id.equals(BigDecimal.ZERO)) {
            return of(thread.intValue());
        }

        return new CommentId(thread.abs().intValue(), id.abs().intValue(), thread.signum() < 0);
    }

    private static class Codec implements Decoder<CommentId>, Encoder<CommentId> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(CommentId value) {
            return value.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CommentId decode(String value) {
            return of(new BigDecimal(value));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean raw() {
            return true;
        }
    }
}
