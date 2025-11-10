package com.example.chatbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final String uploadDir = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            return ResponseEntity.ok("/files/" + fileName);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("File upload failed");
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            byte[] fileContent = Files.readAllBytes(filePath);
            return ResponseEntity.ok(fileContent);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
