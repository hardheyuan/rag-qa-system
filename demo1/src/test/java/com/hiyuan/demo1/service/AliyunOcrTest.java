package com.hiyuan.demo1.service;

import com.hiyuan.demo1.util.PdfImageExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 阿里云 OCR 功能测试
 * 
 * 测试目标：
 * 1. 验证 AppCode 是否有效
 * 2. 验证网络连接是否正常
 * 3. 验证图片识别功能是否正常
 * 4. 验证响应解析是否正确
 */
@SpringBootTest
public class AliyunOcrTest {

    @Autowired
    private AliyunOcrService aliyunOcrService;

    @Autowired
    private PdfImageExtractor pdfImageExtractor;

    @Test
    void testOcrServiceStatus() {
        System.out.println("========== OCR 服务状态测试 ==========");
        boolean enabled = aliyunOcrService.isEnabled();
        System.out.println("OCR 服务启用状态: " + enabled);
        System.out.println("最大处理页数: " + aliyunOcrService.getMaxPagesPerPdf());
        assertTrue(enabled, "OCR 服务应该已启用");
    }

    @Test
    void testImageOcr() throws Exception {
        System.out.println("\n========== 图片 OCR 识别测试 ==========");
        
        // 使用项目中的测试图片
        Path imagePath = Paths.get("D:\\desktop\\myProject\\Snipaste_2026-02-02_15-02-21.png");
        
        System.out.println("测试图片路径: " + imagePath);
        System.out.println("图片是否存在: " + imagePath.toFile().exists());
        
        assertTrue(imagePath.toFile().exists(), "测试图片必须存在");
        
        // 加载图片
        BufferedImage image = ImageIO.read(imagePath.toFile());
        assertNotNull(image, "图片加载失败");
        
        System.out.println("图片尺寸: " + image.getWidth() + "x" + image.getHeight());
        System.out.println("正在调用阿里云 OCR API...");
        System.out.println("（这可能需要几秒钟，请等待...）");
        
        try {
            // 通过反射调用私有方法进行测试
            java.lang.reflect.Method method = AliyunOcrService.class.getDeclaredMethod(
                "recognizeImage", BufferedImage.class);
            method.setAccessible(true);
            String result = (String) method.invoke(aliyunOcrService, image);
            
            System.out.println("\n========== OCR 识别结果 ==========");
            System.out.println("识别到文字长度: " + (result != null ? result.length() : 0) + " 字符");
            System.out.println("识别内容预览:");
            System.out.println(result != null && !result.isEmpty() ? result.substring(0, Math.min(200, result.length())) : "【无内容】");
            
            assertNotNull(result, "OCR 结果不应为 null");
            assertFalse(result.isEmpty(), "OCR 应该能识别到一些文字");
            
            System.out.println("\n✅ OCR 测试通过！服务正常工作");
            
        } catch (Exception e) {
            System.err.println("\n❌ OCR 测试失败！");
            System.err.println("错误类型: " + e.getClass().getSimpleName());
            System.err.println("错误信息: " + e.getMessage());
            
            // 打印详细错误堆栈
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("\n原因: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                cause = cause.getCause();
            }
            
            throw e;
        }
    }

    @Test
    void testNeedsOcrLogic() {
        System.out.println("\n========== 需要 OCR 判断逻辑测试 ==========");
        
        // 测试空文本
        boolean needsOcr1 = aliyunOcrService.needsOcr(null);
        System.out.println("空文本需要 OCR: " + needsOcr1 + " (预期: true)");
        assertTrue(needsOcr1, "空文本应该需要 OCR");
        
        // 测试少文字文本
        boolean needsOcr2 = aliyunOcrService.needsOcr("短文本");
        System.out.println("短文本需要 OCR: " + needsOcr2 + " (预期: true)");
        assertTrue(needsOcr2, "少于 200 字符的文本应该需要 OCR");
        
        // 测试充足文字
        String longText = "这是一段很长的文本。".repeat(20);
        boolean needsOcr3 = aliyunOcrService.needsOcr(longText);
        System.out.println("长文本需要 OCR: " + needsOcr3 + " (预期: false)");
        assertFalse(needsOcr3, "超过 200 字符的文本不应该需要 OCR");
        
        System.out.println("✅ 判断逻辑测试通过");
    }
}
