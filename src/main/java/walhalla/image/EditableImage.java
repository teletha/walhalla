/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package walhalla.image;

import static walhalla.image.Scalr.Method.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import kiss.I;

/**
 * @version 2016/09/25 17:08:32
 */
public class EditableImage {

    private static final Map<Key, Object> hints = new HashMap();

    static {
        hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    }

    /** The original file. */
    private final Path file;

    /** The current image. */
    private BufferedImage image;

    /**
     * 
     */
    public EditableImage() {
        file = null;
        image = new EmptyImage();
    }

    /**
     * @param file
     */
    public EditableImage(Path file) {
        try {
            this.file = file;
            this.image = ImageIO.read(file.toFile());
        } catch (Exception e) {
            throw new RuntimeException(file.toAbsolutePath() + " is not found.");
        }
    }

    /**
     * <p>
     * Triming.
     * </p>
     * 
     * @param size
     */
    public EditableImage trim(int size) {
        return trim(size, size, size, size);
    }

    /**
     * <p>
     * Trim image.
     * </p>
     * 
     * @param top A triming pixel size of top side.
     * @param right A triming pixel size of right side.
     * @param bottom A triming pixel size of bottom side.
     * @param left A triming pixel size of left side.
     */
    public EditableImage trim(int top, int right, int bottom, int left) {
        int width = image.getWidth();
        int height = image.getHeight();

        image = image.getSubimage(left, top, width - left - right, height - top - bottom);

        return this;
    }

    /**
     * <p>
     * Resize image by horizontal length with keeping aspect ratio using Scalr for better quality.
     * </p>
     * 
     * @param size The target size for resizing.
     */
    public EditableImage resize(int size) {
        image = Scalr.resize(image, QUALITY, Scalr.Mode.AUTOMATIC, size, size);
        return this;
    }

    /**
     * <p>
     * Concatenate horizontaly.
     * </p>
     */
    public EditableImage concat(EditableImage target) {
        BufferedImage screen = new BufferedImage(image.getWidth() + target.image.getWidth(), Math
                .max(image.getHeight(), target.image.getHeight()), image.getType());
        Graphics2D graphics = screen.createGraphics();
        graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        graphics.drawImage(target.image, image.getWidth(), 0, target.image.getWidth(), target.image.getHeight(), null);
        image = screen;

        return this;
    }

    /**
     * <p>
     * Arrange images in a tile layout with the specified number of horizontal images per row.
     * </p>
     * 
     * @param horizontalCount The number of images per row.
     * @param images The list of images to arrange in a tile layout.
     */
    public EditableImage tile(int horizontalCount, List<EditableImage> images) {
        if (horizontalCount <= 0 || images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Invalid horizontalCount or images.");
        }

        int tileWidth = 0;
        int tileHeight = 0;
        int rows = (int) Math.ceil((double) images.size() / horizontalCount);

        // Calculate the dimensions of the tiled image
        for (EditableImage image : images) {
            tileWidth = Math.max(tileWidth, image.image.getWidth());
            tileHeight = Math.max(tileHeight, image.image.getHeight());
        }

        int totalWidth = tileWidth * horizontalCount;
        int totalHeight = tileHeight * rows;

        // Ensure the new image supports transparency
        BufferedImage tiledImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tiledImage.createGraphics();
        graphics.setRenderingHints(hints);

        // Draw each image in the correct position
        for (int i = 0; i < images.size(); i++) {
            int x = (i % horizontalCount) * tileWidth;
            int y = (i / horizontalCount) * tileHeight;
            graphics.drawImage(images.get(i).image, x, y, null);
        }

        graphics.dispose();
        this.image = tiledImage;

        return this;
    }

    /**
     * <p>
     * Write current image.
     * </p>
     * 
     * @param output
     */
    public void write(Path output) {
        if (output != null && image != null) {
            String[] names = call(output);

            switch (names[1].toLowerCase()) {
            case "jpg":
            case "jpeg":
                writeJPG(output);
                break;

            case "png":
                writePNG(output);
                break;

            default:
                break;
            }
        }
    }

    /**
     * <p>
     * Write current image.
     * </p>
     * 
     * @param type
     */
    public void write(Path directory, ImageType type) {
        if (directory != null && type != null && image != null) {
            write(directory.resolve(call(file)[0] + "." + type.name().toLowerCase()));
        }
    }

    /**
     * <p>
     * Helper method to write jpeg file.
     * </p>
     * 
     * @param path
     */
    private void writeJPG(Path path) {
        try {
            BufferedImage screen = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            screen.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);

            JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.9f);

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ImageIO.createImageOutputStream(path.toFile()));
            writer.write(null, new IIOImage(screen, null, null), param);
            writer.dispose();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to write PNG file.
     * </p>
     * 
     * @param path The path to save the PNG file.
     */
    private void writePNG(Path path) {
        try {
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2013/03/26 0:27:37
     */
    private static class EmptyImage extends BufferedImage {

        /**
         * 
         */
        private EmptyImage() {
            super(1, 1, BufferedImage.TYPE_INT_RGB);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getWidth() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getHeight() {
            return 0;
        }

    }

    /**
     * <p>
     * Retrieve file name and extension from the specified path.
     * </p>
     * 
     * @param path A target path.
     * @return A file name array like the following [name, extension].
     * @throws NullPointerException A path is <code>null</code>.
     */
    private static String[] call(Path path) {
        String[] names = {"", ""};
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');

        if (index == -1) {
            names[0] = name;
        } else {
            names[0] = name.substring(0, index);
            names[1] = name.substring(index + 1);
        }
        return names;
    }
}