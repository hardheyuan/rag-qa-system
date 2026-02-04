package com.hiyuan.demo1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 阿里云 OCR 简易测试工具（纯 Java，无外部依赖）
 * 使用 Java 内置 HttpClient 测试 OCR API 连接
 */
public class SimpleOcrTest {

    // 阿里云 OCR 配置
    private static final String APPCODE = "946c53ec1f0c4d09aada6d38abedf098";
    private static final String API_URL = "https://gjbsb.market.alicloudapi.com/ocrservice/advanced";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   阿里云 OCR 连接测试");
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
                System.err.println("❌ 错误：无法加载图片");
                System.exit(1);
            }
            System.out.println("✅ 图片加载成功: " + image.getWidth() + "x" + image.getHeight() + "\n");

            // 转换为 Base64
            System.out.println("[3/4] 转换为 Base64...");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            System.out.println("✅ Base64 编码完成，长度: " + base64Image.length() + "\n");

            // 调用 API
            System.out.println("[4/4] 调用阿里云 OCR API...");
            System.out.println("   URL: " + API_URL);
            System.out.println("   预计耗时: 5-15 秒\n");

            String result = callOcrApi(base64Image);

            System.out.println("========================================");
            System.out.println("   ✅ OCR 测试成功！");
            System.out.println("========================================");
            System.out.println("识别长度: " + result.length() + " 字符");
            System.out.println("\n识别内容:");
            System.out.println("----------------------------------------");
            System.out.println(result.isEmpty() ? "【无文字内容】" : result.substring(0, Math.min(500, result.length())));
            if (result.length() > 500) {
                System.out.println("... (还有 " + (result.length() - 500) + " 字符)");
            }
            System.out.println("----------------------------------------");

        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("   ❌❌❌ OCR 测试失败！");
            System.err.println("========================================");
            System.err.println("\n错误类型: " + e.getClass().getSimpleName());
            System.err.println("错误信息: " + e.getMessage());

            // 错误分析
            System.err.println("\n可能原因:");
            if (e.getMessage() != null) {
                if (e.getMessage().contains("401")) {
                    System.err.println("   1. AppCode 无效或已过期");
                    System.err.println("   2. 阿里云账户余额不足");
                    System.err.println("   3. OCR 服务未开通");
                } else if (e.getMessage().contains("403")) {
                    System.err.println("   1. 没有 OCR 接口调用权限");
                    System.err.println("   2. 阿里云市场购买的服务已到期");
                } else if (e.getMessage().contains("404")) {
                    System.err.println("   1. API 路径错误");
                    System.err.println("   2. 服务地址已变更");
                } else if (e.getMessage().contains("ConnectException") || e.getMessage().contains("SocketTimeout")) {
                    System.err.println("   1. 网络连接失败");
                    System.err.println("   2. 防火墙阻止了连接");
                    System.err.println("   3. 阿里云服务器暂时不可用");
                } else if (e.getMessage().contains("400")) {
                    System.err.println("   1. 图片格式不正确");
                    System.err.println("   2. Base64 编码错误");
                    System.err.println("   3. 图片太大");
                }
            }

            System.err.println("\n详细堆栈:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String callOcrApi(String base64Image) throws Exception {
        // 构建 JSON 请求体
        String jsonBody = "{"
            + "\"img\":\"" + base64Image + "\","
            + "\"prob\":false,"
            + "\"charInfo\":false,"
            + "\"rotate\":false,"
            + "\"table\":false"
            + "}";

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "APPCODE " + APPCODE);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);  // 30秒连接超时
        conn.setReadTimeout(60000);     // 60秒读取超时

        // 发送请求
        long startTime = System.currentTimeMillis();
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("   HTTP 状态码: " + responseCode);
        System.out.println("   响应时间: " + duration + "ms\n");

        // 读取响应
        InputStream is = (responseCode >= 200 && responseCode < 300) 
            ? conn.getInputStream() 
            : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != 200) {
            System.err.println("   错误响应: " + response.toString().substring(0, Math.min(500, response.length())));
            throw new RuntimeException("API 调用失败: HTTP " + responseCode);
        }

        return parseResponse(response.toString());
    }

    private static String parseResponse(String json) {
        StringBuilder result = new StringBuilder();
        
        // 简单解析：查找 "word" 字段
        int index = 0;
        while ((index = json.indexOf("\"word\":\"", index)) != -1) {
            index += 8; // 跳过 "word":"
            int endIndex = json.indexOf("\"", index);
            if (endIndex > index) {
                String word = json.substring(index, endIndex);
                result.append(word);
            }
        }
        
        return result.toString().trim();
    }
}
