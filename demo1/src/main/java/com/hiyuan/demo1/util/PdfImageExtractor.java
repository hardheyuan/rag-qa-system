package com.hiyuan.demo1.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 图片提取工具类
 * 
 * 将 PDF 文件转换为图片列表，用于 OCR 文字识别
 * 
 * 技术特点：
 * 1. 支持自定义 DPI（分辨率），平衡质量和速度
 * 2. 自动缩放图片，控制文件大小
 * 3. 内存优化，避免大 PDF 导致内存溢出
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
@Component
public class PdfImageExtractor {

    /**
     * 默认 DPI（每英寸点数）
     * 
     * 150 DPI 是 OCR 的最佳平衡点：
     * - 72 DPI：文件小，但识别率低
     * - 150 DPI：文件适中，识别率 95%+
     * - 300 DPI：文件大，识别率 98%+（但 API 传输慢）
     */
    private static final int DEFAULT_DPI = 150;

    /**
     * 最大图片宽度（像素）
     * 
     * 限制图片大小，避免：
     * 1. 阿里云 OCR API 请求体过大（限制 4MB）
     * 2. 内存溢出
     * 3. 传输时间过长
     */
    private static final int MAX_IMAGE_WIDTH = 2000;

    /**
     * 最大图片高度（像素）
     */
    private static final int MAX_IMAGE_HEIGHT = 3000;

    /**
     * 从 PDF 提取图片
     * 
     * @param pdfPath PDF 文件路径
     * @return 图片列表（每页一张）
     * @throws IOException 当文件读取失败时
     */
    public List<BufferedImage> extractImagesFromPdf(Path pdfPath) throws IOException {
        return extractImagesFromPdf(pdfPath, DEFAULT_DPI);
    }

    /**
     * 从 PDF 提取图片（指定 DPI）
     * 
     * @param pdfPath PDF 文件路径
     * @param dpi DPI（每英寸点数），建议 150-300
     * @return 图片列表（每页一张）
     * @throws IOException 当文件读取失败时
     */
    public List<BufferedImage> extractImagesFromPdf(Path pdfPath, int dpi) throws IOException {
        log.info("开始提取 PDF 图片: {}, DPI: {}", pdfPath.getFileName(), dpi);

        List<BufferedImage> images = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            log.debug("PDF 共 {} 页", pageCount);

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                try {
                    // 渲染页面为图片
                    BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi);

                    // 缩放图片（如果太大）
                    BufferedImage scaledImage = scaleImageIfNeeded(image);

                    images.add(scaledImage);
                    log.debug("第 {} 页提取完成，尺寸: {}x{}", 
                            pageIndex + 1, scaledImage.getWidth(), scaledImage.getHeight());

                } catch (Exception e) {
                    log.error("第 {} 页提取失败: {}", pageIndex + 1, e.getMessage());
                    // 继续处理下一页，不中断整个流程
                }
            }
        }

        log.info("PDF 图片提取完成: {} 页，共 {} 张图片", 
                images.size(), images.size());
        return images;
    }

    /**
     * 缩放图片（如果超过最大尺寸）
     * 
     * @param originalImage 原始图片
     * @return 缩放后的图片（如果需要）
     */
    private BufferedImage scaleImageIfNeeded(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 检查是否需要缩放
        if (originalWidth <= MAX_IMAGE_WIDTH && originalHeight <= MAX_IMAGE_HEIGHT) {
            return originalImage;
        }

        // 计算缩放比例
        double widthRatio = (double) MAX_IMAGE_WIDTH / originalWidth;
        double heightRatio = (double) MAX_IMAGE_HEIGHT / originalHeight;
        double scaleRatio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scaleRatio);
        int newHeight = (int) (originalHeight * scaleRatio);

        log.debug("图片缩放: {}x{} -> {}x{} (比例: {})", 
                originalWidth, originalHeight, newWidth, newHeight, scaleRatio);

        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaledImage.createGraphics();

        // 使用高质量缩放
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        return scaledImage;
    }

    /**
     * 获取 PDF 页数
     * 
     * @param pdfPath PDF 文件路径
     * @return 页数
     * @throws IOException 当文件读取失败时
     */
    public int getPageCount(Path pdfPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            return document.getNumberOfPages();
        }
    }

    /**
     * 估算处理后的图片大小（用于日志和调试）
     * 
     * @param image 图片
     * @return 估算大小（字节）
     */
    public long estimateImageSize(BufferedImage image) {
        // PNG 压缩后大约是原始像素数据的 1/3
        int pixels = image.getWidth() * image.getHeight();
        return pixels * 3L / 3; // RGB，压缩后约 1 字节/像素
    }
}