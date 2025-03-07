package com.itheima._0luogpt.controller;

import com.google.gson.Gson;
import com.itheima._0luogpt.pojo.ImageRecord;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.itheima._0luogpt.pojo.APIResponse;
import com.itheima._0luogpt.pojo.DeeseekRequest;
import com.itheima._0luogpt.pojo.Result;
import com.itheima._0luogpt.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AIController {

    private final Gson gson = new Gson();

    @Autowired
    private ImageService imageService;

    @PostMapping("/tall")
    public ResponseEntity<Result<String>> tallQuestion(@RequestBody String question) throws IOException, UnirestException {
        Unirest.setTimeouts(0, 0);

        List<DeeseekRequest.Message> messages = new ArrayList<>();
        messages.add(DeeseekRequest.Message.builder().role("system").content("你是一个语言学家").build());
        messages.add(DeeseekRequest.Message.builder().role("user").content(question).build());

        DeeseekRequest requestBody = DeeseekRequest.builder()
                .model("deepseek-chat")
                .messages(messages)
                .build();

        HttpResponse<String> response = Unirest.post("https://api.deepseek.com/chat/completions")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer sk-635df622f2ac47408c39642c0fb5d590")
                .body(gson.toJson(requestBody))
                .asString();

        // 解析API响应
        APIResponse apiResponse = gson.fromJson(response.getBody(), APIResponse.class);
        String content = apiResponse.getChoices().get(0).getMessage().getContent();

        // 保存用户的提问和AI的回答到数据库
        ImageRecord record = new ImageRecord();
        record.setUserPrompt(question);
        record.setAiResponse(content);
        record.setGeneratedAt(new Timestamp(System.currentTimeMillis()));

        // 假设有一个方法来保存问答记录
        imageService.saveQuestionToDatabase(record);

        // 返回Result对象
        return new ResponseEntity<>(Result.success(content), HttpStatus.OK);
    }
}
