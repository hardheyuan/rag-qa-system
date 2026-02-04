package com.hiyuan.demo1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * MRL (Matryoshka Representation Learning) 服务
 * 
 * MRL 允许将高维向量截断到较低维度，同时保持大部分语义信息
 * 这样可以在存储空间和检索性能之间取得平衡
 * 
 * 支持的维度：256, 512, 1024, 2048
 */
@Slf4j
@Service
public class MrlService {

    @Value("${embedding.target-dimension:2048}")
    private int targetDimension;

    @Value("${embedding.original-dimension:4096}")
    private int originalDimension;

    /**
     * 截断向量到目标维度
     * 
     * @param fullVector 完整的向量（例如 4096 维）
     * @return 截断后的向量（例如 2048 维）
     */
    public float[] truncateVector(float[] fullVector) {
        if (fullVector == null) {
            throw new IllegalArgumentException("向量不能为空");
        }

        if (fullVector.length < targetDimension) {
            log.warn("向量维度 {} 小于目标维度 {}，返回原向量", fullVector.length, targetDimension);
            return fullVector;
        }

        if (fullVector.length == targetDimension) {
            return fullVector;
        }

        // MRL 的核心：直接截取前 N 维
        float[] truncated = Arrays.copyOf(fullVector, targetDimension);
        
        log.debug("向量截断: {} -> {} 维", fullVector.length, targetDimension);
        return truncated;
    }

    /**
     * 验证目标维度是否有效
     * MRL 通常支持的维度：256, 512, 1024, 2048
     */
    public boolean isValidDimension(int dimension) {
        return dimension == 256 || dimension == 512 || dimension == 1024 || dimension == 2048;
    }

    /**
     * 获取当前配置的目标维度
     */
    public int getTargetDimension() {
        return targetDimension;
    }

    /**
     * 获取原始向量维度
     */
    public int getOriginalDimension() {
        return originalDimension;
    }

    /**
     * 计算维度压缩比
     */
    public double getCompressionRatio() {
        return (double) targetDimension / originalDimension;
    }
}
