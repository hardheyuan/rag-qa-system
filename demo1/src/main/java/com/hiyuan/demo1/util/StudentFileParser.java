package com.hiyuan.demo1.util;

import com.hiyuan.demo1.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生文件解析工具类
 * 
 * 支持解析CSV和Excel格式的学生列表文件，提取学生标识符（用户名或邮箱）。
 * 
 * 支持的文件格式：
 * - CSV文件（.csv）：逗号分隔值，每行一个学生标识符
 * - Excel文件（.xlsx）：第一列为学生标识符
 * 
 * 文件格式要求：
 * - CSV文件：每行包含一个学生标识符（用户名或邮箱），可以有标题行
 * - Excel文件：第一列（A列）包含学生标识符，可以有标题行
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @Autowired
 * private StudentFileParser studentFileParser;
 * 
 * List<String> identifiers = studentFileParser.parseStudentFile(multipartFile);
 * }
 * </pre>
 * 
 * @author 开发团队
 * @version 1.0.0
 * 
 * Requirements: 7.1
 */
@Slf4j
@Component
public class StudentFileParser {

    /**
     * 解析学生文件，提取学生标识符列表
     * 
     * 根据文件扩展名自动选择解析方式：
     * - .xlsx 文件使用 Excel 解析器
     * - 其他文件（包括 .csv）使用 CSV 解析器
     * 
     * @param file 上传的文件（MultipartFile）
     * @return 学生标识符列表（用户名或邮箱）
     * @throws BusinessException 当文件为空、格式不支持或解析失败时抛出
     * 
     * Requirements: 7.1
     */
    public List<String> parseStudentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            throw new BusinessException("文件不能为空");
        }

        String filename = file.getOriginalFilename();
        log.info("开始解析学生文件: {}", filename);

        try {
            List<String> identifiers;
            if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
                identifiers = parseExcelFile(file.getInputStream());
            } else if (filename != null && filename.toLowerCase().endsWith(".xls")) {
                // 不支持旧版 .xls 格式
                throw new BusinessException("不支持 .xls 格式，请使用 .xlsx 或 .csv 格式");
            } else {
                // 默认按 CSV 格式解析
                identifiers = parseCsvFile(file.getInputStream());
            }

            log.info("成功解析学生文件 {}, 共提取 {} 个标识符", filename, identifiers.size());
            return identifiers;

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("解析文件失败: {}", filename, e);
            throw new BusinessException("文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析 CSV 文件
     * 
     * CSV 文件格式要求：
     * - 每行包含一个学生标识符（用户名或邮箱）
     * - 如果行包含逗号，取第一个逗号前的内容作为标识符
     * - 自动跳过空行和只包含空白字符的行
     * - 自动跳过常见的标题行（如 "username", "email", "用户名", "邮箱" 等）
     * 
     * @param inputStream 文件输入流
     * @return 学生标识符列表
     * @throws IOException 当读取文件失败时抛出
     * 
     * Requirements: 7.1
     */
    List<String> parseCsvFile(InputStream inputStream) throws IOException {
        List<String> identifiers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 去除首尾空白
                String trimmedLine = line.trim();
                
                // 跳过空行
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                // 处理 CSV 格式：如果有逗号，取第一列
                String identifier = extractFirstColumn(trimmedLine);
                
                // 去除可能的引号
                identifier = removeQuotes(identifier);
                
                // 跳过空标识符
                if (identifier.isEmpty()) {
                    continue;
                }

                // 跳过标题行
                if (lineNumber == 1 && isHeaderRow(identifier)) {
                    log.debug("跳过标题行: {}", identifier);
                    continue;
                }

                identifiers.add(identifier);
            }
        }

        if (identifiers.isEmpty()) {
            throw new BusinessException("文件中没有有效的学生标识符");
        }

        return identifiers;
    }

    /**
     * 解析 Excel 文件（.xlsx 格式）
     * 
     * Excel 文件格式要求：
     * - 读取第一个工作表
     * - 第一列（A列）包含学生标识符
     * - 自动跳过空行和空单元格
     * - 自动跳过常见的标题行
     * 
     * @param inputStream 文件输入流
     * @return 学生标识符列表
     * @throws IOException 当读取文件失败时抛出
     * 
     * Requirements: 7.1
     */
    List<String> parseExcelFile(InputStream inputStream) throws IOException {
        List<String> identifiers = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet == null) {
                throw new BusinessException("Excel 文件中没有工作表");
            }

            int rowNumber = 0;
            
            for (Row row : sheet) {
                rowNumber++;
                
                // 获取第一列单元格
                Cell cell = row.getCell(0);
                
                if (cell == null) {
                    continue;
                }

                // 获取单元格值
                String identifier = getCellValueAsString(cell);
                
                // 去除首尾空白
                identifier = identifier.trim();
                
                // 跳过空标识符
                if (identifier.isEmpty()) {
                    continue;
                }

                // 跳过标题行
                if (rowNumber == 1 && isHeaderRow(identifier)) {
                    log.debug("跳过标题行: {}", identifier);
                    continue;
                }

                identifiers.add(identifier);
            }
        }

        if (identifiers.isEmpty()) {
            throw new BusinessException("Excel 文件中没有有效的学生标识符");
        }

        return identifiers;
    }

    /**
     * 从 CSV 行中提取第一列
     * 
     * @param line CSV 行
     * @return 第一列的值
     */
    private String extractFirstColumn(String line) {
        // 处理带引号的 CSV 格式
        if (line.startsWith("\"")) {
            int endQuote = line.indexOf("\"", 1);
            if (endQuote > 0) {
                return line.substring(1, endQuote);
            }
        }
        
        // 普通 CSV 格式：取第一个逗号前的内容
        int commaIndex = line.indexOf(',');
        if (commaIndex > 0) {
            return line.substring(0, commaIndex).trim();
        }
        
        return line;
    }

    /**
     * 去除字符串首尾的引号
     * 
     * @param value 原始字符串
     * @return 去除引号后的字符串
     */
    private String removeQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    /**
     * 判断是否为标题行
     * 
     * 常见的标题行关键词：
     * - 英文：username, email, user, student, identifier, id
     * - 中文：用户名, 邮箱, 学生, 标识符
     * 
     * 注意：只有当值完全等于关键词时才认为是标题行，
     * 避免将包含关键词的实际数据（如 student1@example.com）误判为标题行。
     * 
     * @param value 单元格值
     * @return 如果是标题行返回 true
     */
    private boolean isHeaderRow(String value) {
        String lowerValue = value.toLowerCase().trim();
        
        // 常见的标题行关键词 - 使用精确匹配
        String[] headerKeywords = {
            "username", "email", "user", "student", "identifier", "id",
            "用户名", "邮箱", "学生", "标识符", "账号", "账户",
            "name", "姓名", "学号", "student_id", "user_id"
        };
        
        for (String keyword : headerKeywords) {
            // 精确匹配：值必须完全等于关键词
            if (lowerValue.equals(keyword)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取单元格值作为字符串
     * 
     * 支持的单元格类型：
     * - STRING：直接返回字符串值
     * - NUMERIC：转换为字符串（去除小数点后的 .0）
     * - BOOLEAN：转换为 "true" 或 "false"
     * - FORMULA：获取公式计算结果
     * - 其他类型：返回空字符串
     * 
     * @param cell Excel 单元格
     * @return 单元格值的字符串表示
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        
        // 如果是公式，获取公式计算结果的类型
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 将数字转换为字符串，去除小数点后的 .0
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
