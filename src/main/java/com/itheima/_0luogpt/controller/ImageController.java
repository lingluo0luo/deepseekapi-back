package com.itheima._0luogpt.controller;

import com.itheima._0luogpt.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/generate-image")
    public ResponseEntity<byte[]> generateImage(@RequestBody String userPrompt) {
        try {
            System.out.println("POST Body: " + userPrompt);
            byte[] imageData = imageService.generateImage(userPrompt);
            imageService.saveImageToDatabase(userPrompt, imageData, userPrompt, null); // 保存图片到数据库，imageRequest 和 aiResponse 为 userPrompt 和 null
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
