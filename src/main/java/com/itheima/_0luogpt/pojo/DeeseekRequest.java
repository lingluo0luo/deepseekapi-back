package com.itheima._0luogpt.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeeseekRequest {
    private String model;
    private List<Message> messages;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}