package com.DEDUACI.demo.utility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageEncryptor {

    /* ---------- IMAGE CREATION ---------- */
    public static BufferedImage createBlankImage(int textLength) {
        int totalBits = (textLength + 4) * 8; // text + length
        int size = (int) Math.ceil(Math.sqrt(totalBits));
        return new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    }

    /* ---------- ENCRYPT ---------- */
    public static BufferedImage encryptTextWithLength(BufferedImage image, String text) {
        byte[] textBytes = text.getBytes();
        int textLength = textBytes.length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write((textLength >> 24) & 0xFF);
        baos.write((textLength >> 16) & 0xFF);
        baos.write((textLength >> 8) & 0xFF);
        baos.write(textLength & 0xFF);
        baos.writeBytes(textBytes);

        byte[] data = baos.toByteArray();
        int bitIndex = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                if (bitIndex >= data.length * 8) return image;

                int byteIndex = bitIndex / 8;
                int bitInByte = 7 - (bitIndex % 8); // MSB → LSB
                int bit = (data[byteIndex] >> bitInByte) & 1;

                int rgb = image.getRGB(x, y);
                int blue = (rgb & 0xFF);
                blue = (blue & 0xFE) | bit;

                image.setRGB(x, y, blue);
                bitIndex++;
            }
        }
        return image;
    }

    /* ---------- DECRYPT ---------- */
    public static String decryptTextWithLength(BufferedImage image) {

        int bitIndex = 0;
        int textLength = 0;

        /* ---- Read length (32 bits) ---- */
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                int bit = image.getRGB(x, y) & 1;
                textLength = (textLength << 1) | bit;
                bitIndex++;

                if (bitIndex == 32) break;
            }
            if (bitIndex == 32) break;
        }

        if (textLength <= 0 || textLength > 10000)
            throw new IllegalArgumentException("Invalid encrypted image");

        /* ---- Read text ---- */
        byte[] textBytes = new byte[textLength];
        int currentByte = 0;
        int bitsCollected = 0;
        int bytePos = 0;

        int pixelCount = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                pixelCount++;
                if (pixelCount <= 32) continue; // skip length bits

                int bit = image.getRGB(x, y) & 1;
                currentByte = (currentByte << 1) | bit;
                bitsCollected++;

                if (bitsCollected == 8) {
                    textBytes[bytePos++] = (byte) currentByte;
                    currentByte = 0;
                    bitsCollected = 0;
                    if (bytePos == textLength) {
                        return new String(textBytes);
                    }
                }
            }
        }
        return new String(textBytes);
    }

    /* ---------- IO ---------- */
    public static BufferedImage readImage(InputStream input) throws IOException {
        return ImageIO.read(input);
    }

    public static void writeImage(BufferedImage image, OutputStream output, String format) throws IOException {
        ImageIO.write(image, format, output);
    }
}
