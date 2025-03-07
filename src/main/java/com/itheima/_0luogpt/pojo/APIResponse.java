package com.itheima._0luogpt.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse {
    private String id;
    private String object;
    private int created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private String system_fingerprint;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private int index;
        private Message message;
        private Object logprobs;
        private String finish_reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
        private Map<String, Integer> prompt_tokens_details;
        private int prompt_cache_hit_tokens;
        private int prompt_cache_miss_tokens;
    }
}
