package com.DEDUACI.demo.controller;

import com.DEDUACI.demo.utility.CryptoUtil;
import com.DEDUACI.demo.utility.ImageEncryptor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class MainController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    /* ---------- ENCRYPT TEXT → IMAGE ---------- */
    @PostMapping("/encrypt")
    public void encrypt(
            @RequestParam("text") String text,
            @RequestParam("passcode") String passcode,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletResponse response) throws Exception {

        // 1️⃣ AES encrypt text
        byte[] encryptedBytes = CryptoUtil.encrypt(text.getBytes(StandardCharsets.UTF_8), passcode);

        // 2️⃣ Create colored image if no image uploaded
        BufferedImage image;
        if (imageFile != null && !imageFile.isEmpty()) {
            image = ImageEncryptor.readImage(imageFile.getInputStream());
        } else {
            image = ImageEncryptor.createColoredImage(encryptedBytes.length);
        }

        // 3️⃣ Embed encrypted bytes
        BufferedImage encryptedImage = ImageEncryptor.encryptBytesWithLength(image, encryptedBytes);

        // 4️⃣ Send image to browser
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "inline; filename=encrypted.png");

        ImageEncryptor.writeImage(encryptedImage, response.getOutputStream(), "png");
    }

    /* ---------- DECRYPT IMAGE → TEXT ---------- */
    @PostMapping("/decrypt")
    @ResponseBody
    public String decrypt(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("passcode") String passcode) throws IOException {

        BufferedImage image = ImageEncryptor.readImage(imageFile.getInputStream());

        // 1️⃣ Extract encrypted bytes
        byte[] encryptedBytes = ImageEncryptor.decryptBytesWithLength(image);

        // 2️⃣ AES decrypt (fails if wrong passcode)
        try {
            byte[] decrypted = CryptoUtil.decrypt(encryptedBytes, passcode);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "❌ Invalid passcode or corrupted image";
        }
    }
}
