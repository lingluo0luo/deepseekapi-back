package com.itheima._0luogpt.controller;

import com.itheima._0luogpt.pojo.ImageRecord;
import com.itheima._0luogpt.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoryController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/history")
    public ResponseEntity<List<ImageRecord>> getImageHistory() {
        try {
            List<ImageRecord> history = imageService.getImageHistory();
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
