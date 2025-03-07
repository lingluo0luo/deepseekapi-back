package com.itheima._0luogpt.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itheima._0luogpt.pojo.ImageRecord;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ImageService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public byte[] generateImage(String userPrompt) throws IOException {
        // Step 1: 发送生成请求
        String postBody = createPostBody(userPrompt);
        System.out.println("POST Body: " + postBody);

        RequestBody body = RequestBody.create(postBody, JSON);
        Request request = new Request.Builder()
                .url("http://172.17.8.147:8188/prompt")
                .post(body)
                .build();

        String promptId;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("生成请求失败: " + response.body().string());
                throw new IOException("生成请求失败: " + response);
            }

            // 解析prompt_id
            String responseBody = response.body().string();
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
            promptId = jsonObject.get("prompt_id").getAsString();
            System.out.println("任务ID: " + promptId);
        }

        // Step 2: 轮询任务状态（最大重试30次，间隔1秒）
        int maxRetries = 30; // 延长至30次
        int retryCount = 0;
        String filename = null; // 提前声明变量

        while (retryCount < maxRetries && filename == null) { // 添加filename判空条件
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("轮询被中断");
            }

            Request historyRequest = new Request.Builder()
                    .url("http://172.17.8.147:8188/history/" + promptId)
                    .build();

            try (Response historyResponse = client.newCall(historyRequest).execute()) {
                if (!historyResponse.isSuccessful()) continue;

                String historyBody = historyResponse.body().string();
                System.out.println("历史响应原始数据: " + historyBody); // 调试输出

                JsonObject historyData = new Gson().fromJson(historyBody, JsonObject.class);
                if (!historyData.has(promptId)) {
                    retryCount++;
                    continue;
                }

                JsonObject promptData = historyData.getAsJsonObject(promptId);
                JsonObject outputs = promptData.getAsJsonObject("outputs");

                // 关键修复：根据实际数据结构解析
                if (outputs != null && outputs.has("9")) {
                    JsonElement node9 = outputs.get("9");
                    if (node9.isJsonObject()) { // 检查是否为Object
                        JsonObject saveImageData = node9.getAsJsonObject();
                        if (saveImageData.has("images")) {
                            JsonArray images = saveImageData.getAsJsonArray("images");
                            if (images.size() > 0) {
                                filename = images.get(0).getAsJsonObject()
                                        .get("filename").getAsString();
                                System.out.println("成功获取文件名: " + filename);
                                break;
                            }
                        }
                    } else {
                        System.out.println("节点9格式异常，预期Object实际类型: " + node9.getClass());
                    }
                }
                retryCount++;
                System.out.println("第" + retryCount + "次重试...");
            }
        }

        if (filename == null) {
            throw new IOException("图片生成超时，未找到输出文件");
        }

        // Step 3: 获取图片（此时filename已正确赋值）
        Request imageRequest = new Request.Builder()
                .url("http://172.17.8.147:8188/view?filename=" + filename)
                .build();

        try (Response imageResponse = client.newCall(imageRequest).execute()) {
            if (!imageResponse.isSuccessful()) {
                throw new IOException("图片获取失败: " + imageResponse);
            }
            return imageResponse.body().bytes();
        }
    }

    private String createPostBody(String userPrompt) {
        JsonObject body = new JsonObject();
        JsonObject prompt = new JsonObject();

        // 根据JSON格式构建所有节点
        prompt.add("3", buildKSampler());
        prompt.add("4", buildCheckpointLoader());
        prompt.add("5", buildLatentImage());
        prompt.add("6", buildPositiveClip(userPrompt));
        prompt.add("7", buildNegativeClip());
        prompt.add("8", buildVAEDecode());
        prompt.add("9", buildSaveImage());

        body.add("prompt", prompt);
        body.addProperty("client_id", "111");

        return body.toString();
    }

    private JsonObject buildKSampler() {
        JsonObject inputs = new JsonObject();
        inputs.addProperty("seed", 15668);
        inputs.addProperty("steps", 20);
        inputs.addProperty("cfg", 8);
        inputs.addProperty("sampler_name", "euler");
        inputs.addProperty("scheduler", "normal");
        inputs.addProperty("denoise", 1);

        // 构建所有节点连接
        inputs.add("model", buildNodeConnection("4", 0));
        inputs.add("positive", buildNodeConnection("6", 0));
        inputs.add("negative", buildNodeConnection("7", 0));
        inputs.add("latent_image", buildNodeConnection("5", 0));

        return buildNode("KSampler", inputs, "KSampler");
    }

    private JsonObject buildCheckpointLoader() {
        JsonObject inputs = new JsonObject();
        inputs.addProperty("ckpt_name", "dreamshaper_8.safetensors");
        return buildNode("CheckpointLoaderSimple", inputs, "Load Checkpoint");
    }

    private JsonObject buildLatentImage() {
        JsonObject inputs = new JsonObject();
        inputs.addProperty("width", 512);
        inputs.addProperty("height", 512);
        inputs.addProperty("batch_size", 1);
        return buildNode("EmptyLatentImage", inputs, "Empty Latent Image");
    }

    private JsonObject buildPositiveClip(String prompt) {
        JsonObject inputs = new JsonObject();
        inputs.addProperty("text", prompt.replace("\r\n", " "));
        inputs.add("clip", buildNodeConnection("4", 1));
        return buildNode("CLIPTextEncode", inputs, "CLIP Text Encode (Prompt)");
    }

    private JsonObject buildNegativeClip() {
        JsonObject inputs = new JsonObject();
        inputs.addProperty("text", "text, watermark");
        inputs.add("clip", buildNodeConnection("4", 1));
        return buildNode("CLIPTextEncode", inputs, "CLIP Text Encode (Prompt)");
    }

    private JsonObject buildVAEDecode() {
        JsonObject inputs = new JsonObject();
        inputs.add("samples", buildNodeConnection("3", 0));
        inputs.add("vae", buildNodeConnection("4", 2));
        return buildNode("VAEDecode", inputs, "VAE Decode");
    }

    private JsonObject buildSaveImage() {
        JsonObject inputs = new JsonObject();
        inputs.addProperty("filename_prefix", "ComfyUI");
        inputs.add("images", buildNodeConnection("8", 0));
        return buildNode("SaveImage", inputs, "Save Image");
    }

    // 工具方法：构建节点连接数组 ["节点ID", 输出索引]
    private JsonArray buildNodeConnection(String nodeId, int outputIndex) {
        JsonArray arr = new JsonArray();
        arr.add(nodeId);   // 必须为字符串类型
        arr.add(outputIndex);
        return arr;
    }

    // 工具方法：统一构建节点结构
    private JsonObject buildNode(String classType, JsonObject inputs, String title) {
        JsonObject node = new JsonObject();
        node.add("inputs", inputs);
        node.addProperty("class_type", classType);

        JsonObject meta = new JsonObject();
        meta.addProperty("title", title);
        node.add("_meta", meta);

        return node;
    }


    public void saveImageToDatabase(String userPrompt, byte[] imageData, String imageRequest, String aiResponse) {
        String sql = "INSERT INTO image_records (user_prompt, image_data, image_request, ai_response, generated_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userPrompt, imageData, imageRequest, aiResponse, new Timestamp(System.currentTimeMillis()));
    }

    public void saveQuestionToDatabase(ImageRecord record) {
        String sql = "INSERT INTO image_records (user_prompt, ai_response, generated_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, record.getUserPrompt(), record.getAiResponse(), record.getGeneratedAt());
    }

    public List<ImageRecord> getImageHistory() {
        String sql = "SELECT id, user_prompt, image_data, image_request, ai_response, generated_at FROM image_records ORDER BY generated_at ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ImageRecord record = new ImageRecord();
            record.setId(rs.getInt("id"));
            record.setUserPrompt(rs.getString("user_prompt"));
            record.setImageData(rs.getBytes("image_data"));
            record.setImageRequest(rs.getString("image_request"));
            record.setAiResponse(rs.getString("ai_response"));
            record.setGeneratedAt(rs.getTimestamp("generated_at"));
            return record;
        });
    }

}
