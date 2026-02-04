package com.hiyuan.demo1.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 向量工具类
 * <p>
 * 提供通用的向量操作方法，包括：
 * <ul>
 *   <li>向量与字符串之间的相互转换</li>
 *   <li>余弦相似度计算</li>
 *   <li>向量归一化（L2归一化）</li>
 * </ul>
 * <p>
 * 这个工具类是RAG系统的核心组件之一，被多个服务类共享使用。
 *
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
public final class VectorUtils {

    private VectorUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将float数组转换为PostgreSQL向量格式字符串
     * <p>
     * 格式：[v1,v2,v3,...,vn]
     * <p>
     * 示例：
     * <pre>
     * float[] vector = {0.1f, 0.2f, 0.3f};
     * String result = VectorUtils.vectorToString(vector);
     * // 结果: "[0.1,0.2,0.3]"
     * </pre>
     *
     * @param vector float数组向量
     * @return PostgreSQL向量格式字符串
     * @throws IllegalArgumentException 如果vector为null
     */
    public static String vectorToString(float[] vector) {
        if (vector == null) {
            throw new IllegalArgumentException("向量不能为null");
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将PostgreSQL向量格式字符串解析为float数组
     * <p>
     * 支持的格式：[v1,v2,v3,...,vn] 或 v1,v2,v3,...,vn
     * <p>
     * 示例：
     * <pre>
     * String vectorStr = "[0.1,0.2,0.3]";
     * float[] result = VectorUtils.parseVectorString(vectorStr);
     * // 结果: [0.1f, 0.2f, 0.3f]
     * </pre>
     *
     * @param vectorStr PostgreSQL向量格式字符串
     * @return float数组向量
     * @throws IllegalArgumentException 如果vectorStr为null或空白，或格式不正确
     */
    public static float[] parseVectorString(String vectorStr) {
        if (vectorStr == null || vectorStr.isBlank()) {
            throw new IllegalArgumentException("向量字符串不能为空");
        }

        String s = vectorStr.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]")) s = s.substring(0, s.length() - 1);

        if (s.isEmpty()) {
            return new float[0];
        }

        String[] parts = s.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                vector[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    String.format("无法解析向量字符串的第%d个值: '%s'", i, parts[i].trim())
                );
            }
        }
        return vector;
    }

    /**
     * 计算两个向量的余弦相似度
     * <p>
     * 余弦相似度衡量两个向量在方向上的相似程度，范围：-1 到 1
     * <ul>
     *   <li>1：完全相同方向</li>
     *   <li>0：正交（无关）</li>
     *   <li>-1：完全相反方向</li>
     * </ul>
     * <p>
     * 如果两个向量长度不同，只会比较较短长度的部分。
     * <p>
     * 示例：
     * <pre>
     * float[] v1 = {1.0f, 0.0f};
     * float[] v2 = {1.0f, 1.0f};
     * double similarity = VectorUtils.cosineSimilarity(v1, v2);
     * // 结果: 0.707... (约等于 √2/2)
     * </pre>
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度，范围[-1, 1]
     * @throws IllegalArgumentException 如果任一向量为null
     */
    public static double cosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("向量不能为null");
        }

        // 使用较短的长度进行计算
        int len = Math.min(vector1.length, vector2.length);

        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < len; i++) {
            dot += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        double denom = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (denom == 0.0) return 0.0;
        return dot / denom;
    }

    /**
     * 计算两个向量的余弦相似度（带维度检查）
     * <p>
     * 与 {@link #cosineSimilarity(float[], float[])} 类似，但要求两个向量维度必须相同。
     * 如果维度不同，将抛出IllegalArgumentException。
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度，范围[-1, 1]
     * @throws IllegalArgumentException 如果任一向量为null，或维度不匹配
     */
    public static double cosineSimilarityStrict(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("向量不能为null");
        }

        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException(
                String.format("向量维度不匹配: %d vs %d", vector1.length, vector2.length)
            );
        }

        return cosineSimilarity(vector1, vector2);
    }

    /**
     * 对向量进行L2归一化
     * <p>
     * L2归一化将向量缩放到单位长度（长度为1），同时保持方向不变。
     * <p>
     * 公式：v_normalized = v / ||v||<sub>2</sub>
     * <p>
     * 示例：
     * <pre>
     * float[] v = {3.0f, 4.0f};  // 长度为5
     * float[] normalized = VectorUtils.normalize(v);
     * // 结果: [0.6, 0.8] (长度为1)
     * </pre>
     *
     * @param vector 输入向量
     * @return 归一化后的向量（新数组）
     * @throws IllegalArgumentException 如果向量为null或零向量
     */
    public static float[] normalize(float[] vector) {
        if (vector == null) {
            throw new IllegalArgumentException("向量不能为null");
        }

        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }

        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            throw new IllegalArgumentException("零向量无法归一化");
        }

        float[] result = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = (float) (vector[i] / norm);
        }

        return result;
    }

    /**
     * 计算向量的L2范数（欧几里得长度）
     * <p>
     * 公式：||v||<sub>2</sub> = sqrt(sum(v<sub>i</sub><sup>2</sup>))
     *
     * @param vector 输入向量
     * @return L2范数
     * @throws IllegalArgumentException 如果向量为null
     */
    public static double l2Norm(float[] vector) {
        if (vector == null) {
            throw new IllegalArgumentException("向量不能为null");
        }

        double sum = 0.0;
        for (float v : vector) {
            sum += v * v;
        }

        return Math.sqrt(sum);
    }

    /**
     * 验证向量是否有效
     * <p>
     * 检查以下条件：
     * <ul>
     *   <li>向量不为null</li>
     *   <li>向量长度大于0</li>
     *   <li>所有元素都是有效数字（非NaN，非无穷大）</li>
     * </ul>
     *
     * @param vector 要验证的向量
     * @return true 如果向量有效，false 否则
     */
    public static boolean isValidVector(float[] vector) {
        if (vector == null || vector.length == 0) {
            return false;
        }

        for (float v : vector) {
            if (Float.isNaN(v) || Float.isInfinite(v)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 验证两个向量是否具有相同的维度
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return true 如果两个向量维度相同
     */
    public static boolean hasSameDimension(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null) {
            return false;
        }
        return vector1.length == vector2.length;
    }
}
