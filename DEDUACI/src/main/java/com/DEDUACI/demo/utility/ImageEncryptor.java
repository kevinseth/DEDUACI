package com.DEDUACI.demo.utility;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

public class ImageEncryptor {

    /* ---------- CREATE RANDOM COLORED IMAGE ---------- */
	public static BufferedImage createColoredImage(int dataLength) {

	    int totalBits = (dataLength + 4) * 8; // +4 bytes for length
	    int pixelsNeeded = (int) Math.ceil(totalBits / 3.0);

	    // Calculate width & height
	    int width = (int) Math.ceil(Math.sqrt(pixelsNeeded));
	    int height = (int) Math.ceil((double) pixelsNeeded / width);

	    // ✅ Set a minimum size for visibility
	    width = Math.max(width, 200);   // minimum width 200px
	    height = Math.max(height, 200); // minimum height 200px

	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	    Random random = new Random();
	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            int r = random.nextInt(256);
	            int g = random.nextInt(256);
	            int b = random.nextInt(256);
	            image.setRGB(x, y, new Color(r, g, b).getRGB());
	        }
	    }
	    return image;
	}

    /* ---------- ENCRYPT BYTES INTO IMAGE ---------- */
    public static BufferedImage encryptBytesWithLength(
            BufferedImage image, byte[] data) {

        int width = image.getWidth();
        int height = image.getHeight();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Store length (4 bytes)
        bos.write((data.length >> 24) & 0xFF);
        bos.write((data.length >> 16) & 0xFF);
        bos.write((data.length >> 8) & 0xFF);
        bos.write(data.length & 0xFF);

        try {
            bos.write(data);
        } catch (IOException ignored) {}

        byte[] payload = bos.toByteArray();
        int bitIndex = 0;

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (bitIndex >= payload.length * 8)
                    break outer;

                Color color = new Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                r = setLSB(r, getBit(payload, bitIndex++));
                if (bitIndex < payload.length * 8)
                    g = setLSB(g, getBit(payload, bitIndex++));
                if (bitIndex < payload.length * 8)
                    b = setLSB(b, getBit(payload, bitIndex++));

                image.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return image;
    }

    /* ---------- DECRYPT BYTES ---------- */
    public static byte[] decryptBytesWithLength(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int length = 0;
        int lengthBitsRead = 0;

        int currentByte = 0;
        int bitsCollected = 0;
        int bytesRead = 0;

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Color color = new Color(image.getRGB(x, y));
                int[] bits = {
                        color.getRed() & 1,
                        color.getGreen() & 1,
                        color.getBlue() & 1
                };

                for (int bit : bits) {

                    /* ---- READ LENGTH (32 bits) ---- */
                    if (lengthBitsRead < 32) {
                        length = (length << 1) | bit;
                        lengthBitsRead++;
                        continue;
                    }

                    /* ---- READ DATA ---- */
                    currentByte = (currentByte << 1) | bit;
                    bitsCollected++;

                    if (bitsCollected == 8) {
                        bos.write(currentByte);
                        bitsCollected = 0;
                        currentByte = 0;
                        bytesRead++;

                        if (bytesRead == length)
                            break outer;
                    }
                }
            }
        }
        return bos.toByteArray();
    }


    /* ---------- HELPERS ---------- */
    private static int setLSB(int value, int bit) {
        return (value & 0xFE) | bit;
    }

    private static int getBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitInByte = 7 - (bitIndex % 8);
        return (data[byteIndex] >> bitInByte) & 1;
    }

    public static void writeImage(BufferedImage image,
                                  OutputStream out,
                                  String format) throws IOException {
        ImageIO.write(image, format, out);
    }

    public static BufferedImage readImage(InputStream in)
            throws IOException {
        return ImageIO.read(in);
    }
}
