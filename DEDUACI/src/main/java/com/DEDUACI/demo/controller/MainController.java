package com.DEDUACI.demo.controller;

import com.DEDUACI.demo.utility.ImageEncryptor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
public class MainController {

    @GetMapping("/home")
    public String home() {
        return "home"; // Thymeleaf template home.html
    }

    /**
     * Encrypt text into image.
     * If no image is uploaded, creates a blank image.
     */
    @PostMapping("/encrypt")
    public void encrypt(@RequestParam("text") String text,
                        @RequestParam(value = "image", required = false) MultipartFile imageFile,
                        HttpServletResponse response) throws IOException {

        BufferedImage image;

        if (imageFile != null && !imageFile.isEmpty()) {
            image = ImageEncryptor.readImage(imageFile.getInputStream());
        } else {
            // Create a blank image big enough for text
            image = ImageEncryptor.createBlankImage(text.length());
        }

        BufferedImage encrypted = ImageEncryptor.encryptTextWithLength(image, text);

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "inline; filename=encrypted.png");

        ImageEncryptor.writeImage(encrypted, response.getOutputStream(), "png");
    }

    /**
     * Decrypt text from uploaded image.
     * Reads the first 32 bits to get text length.
     */
    @PostMapping("/decrypt")
    @ResponseBody
    public String decrypt(@RequestParam("image") MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return "No image uploaded!";
        }

        BufferedImage image = ImageEncryptor.readImage(imageFile.getInputStream());

        // Decrypt text automatically by reading the stored length
        String decryptedText = ImageEncryptor.decryptTextWithLength(image);

        return decryptedText;
    }
}
