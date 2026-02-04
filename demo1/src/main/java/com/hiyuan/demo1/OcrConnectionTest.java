package com.hiyuan.demo1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云 OCR 简易测试工具
 * 不依赖 Spring Boot，直接测试 OCR API 连接
 */
public class OcrConnectionTest {

    // 阿里云 OCR 配置（从 application.yml 复制）
    private static final String APPCODE = "946c53ec1f0c4d09aada6d38abedf098";
    private static final String HOST = "https://gjbsb.market.alicloudapi.com";
    private static final String PATH = "/ocrservice/advanced";
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   阿里云 OCR 连接测试工具");
        System.out.println("========================================\n");

        // 检查测试图片
        String imagePath = "D:\\desktop\\myProject\\Snipaste_2026-02-02_15-02-21.png";
        File imageFile = new File(imagePath);
        
        System.out.println("[1/4] 检查测试图片...");
        if (!imageFile.exists()) {
            System.err.println("❌ 错误：测试图片不存在: " + imagePath);
            System.exit(1);
        }
        System.out.println("✅ 图片存在: " + imagePath);
        System.out.println("   文件大小: " + (imageFile.length() / 1024) + " KB\n");

        try {
            // 加载图片
            System.out.println("[2/4] 加载图片...");
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                System.err.println("❌ 错误：无法加载图片（可能格式不支持）");
                System.exit(1);
            }
            System.out.println("✅ 图片加载成功");
            System.out.println("   尺寸: " + image.getWidth() + "x" + image.getHeight() + "\n");

            // 转换为 Base64
            System.out.println("[3/4] 转换为 Base64...");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            System.out.println("✅ Base64 编码完成");
            System.out.println("   编码长度: " + base64Image.length() + " 字符\n");

            // 调用 OCR API
            System.out.println("[4/4] 调用阿里云 OCR API...");
            System.out.println("   URL: " + HOST + PATH);
            System.out.println("   正在发送请求（约需 3-10 秒）...\n");
            
            String result = callOcrApi(base64Image);
            
            // 显示结果
            System.out.println("========================================");
            System.out.println("   OCR 识别结果");
            System.out.println("========================================");
            System.out.println("识别长度: " + result.length() + " 字符");
            System.out.println("\n识别内容:");
            System.out.println("----------------------------------------");
            System.out.println(result);
            System.out.println("----------------------------------------\n");
            
            if (result.length() > 0) {
                System.out.println("✅✅✅ OCR 测试成功！API 连接正常！");
                System.out.println("   阿里云 OCR 服务可以正常工作");
            } else {
                System.out.println("⚠️ OCR 返回空结果，但连接正常");
            }

        } catch (Exception e) {
            System.err.println("\n❌❌❌ OCR 测试失败！");
            System.err.println("\n错误信息:");
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            
            // 分析常见错误
            System.err.println("\n可能原因:");
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("401") || message.contains("403")) {
                    System.err.println("   - AppCode 无效或已过期");
                    System.err.println("   - 阿里云账户余额不足");
                } else if (message.contains("404")) {
                    System.err.println("   - API 路径错误");
                } else if (message.contains("Connect") || message.contains("timeout")) {
                    System.err.println("   - 网络连接失败");
                    System.err.println("   - 无法连接到阿里云服务器");
                } else if (message.contains("HTTP 400")) {
                    System.err.println("   - 请求参数错误");
                    System.err.println("   - Base64 图片格式不正确");
                }
            }
            
            System.err.println("\n详细错误堆栈:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 调用阿里云 OCR API
     */
    private static String callOcrApi(String base64Image) throws Exception {
        String url = HOST + PATH;
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("img", base64Image);
        requestBody.put("prob", false);
        requestBody.put("charInfo", false);
        requestBody.put("rotate", false);
        requestBody.put("table", false);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "APPCODE " + APPCODE);
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), "UTF-8");
                
                System.out.println("   HTTP 状态码: " + statusCode);
                
                if (statusCode == 200) {
                    return parseOcrResponse(responseBody);
                } else {
                    System.err.println("   响应内容: " + responseBody.substring(0, Math.min(500, responseBody.length())));
                    throw new RuntimeException("OCR API 调用失败: HTTP " + statusCode);
                }
            }
        }
    }

    /**
     * 解析 OCR API 响应
     */
    private static String parseOcrResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        
        // 检查错误
        if (root.has("error")) {
            String errorMsg = root.path("error").asText();
            throw new RuntimeException("OCR 返回错误: " + errorMsg);
        }
        
        // 检查错误码
        if (root.has("errCode") && !root.path("errCode").asText().isEmpty()) {
            String errCode = root.path("errCode").asText();
            String errMsg = root.path("errMsg").asText("未知错误");
            throw new RuntimeException("OCR 错误 [" + errCode + "]: " + errMsg);
        }
        
        // 提取文字
        StringBuilder text = new StringBuilder();
        JsonNode prismWordsInfo = root.path("prism_wordsInfo");
        
        if (prismWordsInfo.isArray() && prismWordsInfo.size() > 0) {
            String currentLine = "";
            int lastRow = -1;
            
            for (JsonNode wordNode : prismWordsInfo) {
                int row = wordNode.path("row").asInt(-1);
                String word = wordNode.path("word").asText("");
                
                if (row != lastRow) {
                    if (!currentLine.isEmpty()) {
                        text.append(currentLine).append("\n");
                    }
                    currentLine = word;
                    lastRow = row;
                } else {
                    currentLine += word;
                }
            }
            
            if (!currentLine.isEmpty()) {
                text.append(currentLine);
            }
        } else {
            String directText = root.path("text").asText("");
            if (!directText.isEmpty()) {
                text.append(directText);
            }
        }
        
        return text.toString().trim();
    }
}
