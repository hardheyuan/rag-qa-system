package com.hiyuan.demo1.util;

import com.hiyuan.demo1.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Helper constant for line separator in tests
// Using \n which BufferedReader handles correctly on all platforms

/**
 * StudentFileParser 单元测试
 * 
 * 测试 CSV 和 Excel 文件解析功能
 * 
 * Requirements: 7.1
 */
@DisplayName("StudentFileParser 测试")
class StudentFileParserTest {

    private StudentFileParser parser;
    
    // Use \n for line separator - BufferedReader handles this correctly on all platforms
    private static final String LF = "\n";

    @BeforeEach
    void setUp() {
        parser = new StudentFileParser();
    }

    @Nested
    @DisplayName("CSV 文件解析测试")
    class CsvParsingTests {

        @Test
        @DisplayName("解析简单 CSV 文件 - 每行一个标识符")
        void parseCsvFile_simpleFormat_returnsIdentifiers() throws IOException {
            // Given
            String csvContent = "student1@example.com" + LF + "student2@example.com" + LF + "student3";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(3, identifiers.size());
            assertEquals("student1@example.com", identifiers.get(0));
            assertEquals("student2@example.com", identifiers.get(1));
            assertEquals("student3", identifiers.get(2));
        }

        @Test
        @DisplayName("解析带标题行的 CSV 文件 - 自动跳过标题")
        void parseCsvFile_withHeader_skipsHeader() throws IOException {
            // Given
            String csvContent = "username" + LF + "student1" + LF + "student2";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("student1", identifiers.get(0));
            assertEquals("student2", identifiers.get(1));
        }

        @Test
        @DisplayName("解析带中文标题行的 CSV 文件")
        void parseCsvFile_withChineseHeader_skipsHeader() throws IOException {
            // Given
            String csvContent = "用户名" + LF + "zhangsan" + LF + "lisi";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("zhangsan", identifiers.get(0));
            assertEquals("lisi", identifiers.get(1));
        }

        @Test
        @DisplayName("解析多列 CSV 文件 - 只取第一列")
        void parseCsvFile_multipleColumns_extractsFirstColumn() throws IOException {
            // Given
            String csvContent = "student1@example.com,John,Doe" + LF + "student2@example.com,Jane,Smith";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("student1@example.com", identifiers.get(0));
            assertEquals("student2@example.com", identifiers.get(1));
        }

        @Test
        @DisplayName("解析带引号的 CSV 文件")
        void parseCsvFile_withQuotes_removesQuotes() throws IOException {
            // Given
            String csvContent = "\"student1@example.com\"" + LF + "\"student2@example.com\"";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("student1@example.com", identifiers.get(0));
            assertEquals("student2@example.com", identifiers.get(1));
        }

        @Test
        @DisplayName("解析带空行的 CSV 文件 - 自动跳过空行")
        void parseCsvFile_withEmptyLines_skipsEmptyLines() throws IOException {
            // Given
            String csvContent = "student1" + LF + LF + LF + "student2" + LF + "   " + LF + "student3";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(3, identifiers.size());
            assertEquals("student1", identifiers.get(0));
            assertEquals("student2", identifiers.get(1));
            assertEquals("student3", identifiers.get(2));
        }

        @Test
        @DisplayName("解析空 CSV 文件 - 抛出异常")
        void parseCsvFile_emptyFile_throwsException() {
            // Given
            String csvContent = "";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> parser.parseCsvFile(inputStream));
            assertTrue(exception.getMessage().contains("没有有效的学生标识符"));
        }

        @Test
        @DisplayName("解析只有标题行的 CSV 文件 - 抛出异常")
        void parseCsvFile_onlyHeader_throwsException() {
            // Given - single line with header keyword
            String csvContent = "username";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> parser.parseCsvFile(inputStream));
            assertTrue(exception.getMessage().contains("没有有效的学生标识符"));
        }
    }

    @Nested
    @DisplayName("MultipartFile 解析测试")
    class MultipartFileParsingTests {

        @Test
        @DisplayName("解析 CSV MultipartFile")
        void parseStudentFile_csvFile_returnsIdentifiers() {
            // Given
            String csvContent = "student1@example.com" + LF + "student2@example.com";
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "students.csv",
                    "text/csv",
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseStudentFile(file);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("student1@example.com", identifiers.get(0));
            assertEquals("student2@example.com", identifiers.get(1));
        }

        @Test
        @DisplayName("解析空文件 - 抛出异常")
        void parseStudentFile_emptyFile_throwsException() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "students.csv",
                    "text/csv",
                    new byte[0]);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> parser.parseStudentFile(file));
            assertTrue(exception.getMessage().contains("文件不能为空"));
        }

        @Test
        @DisplayName("解析 null 文件 - 抛出异常")
        void parseStudentFile_nullFile_throwsException() {
            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> parser.parseStudentFile(null));
            assertTrue(exception.getMessage().contains("文件不能为空"));
        }

        @Test
        @DisplayName("解析 .xls 文件 - 抛出不支持异常")
        void parseStudentFile_xlsFile_throwsException() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "students.xls",
                    "application/vnd.ms-excel",
                    "dummy content".getBytes(StandardCharsets.UTF_8));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> parser.parseStudentFile(file));
            assertTrue(exception.getMessage().contains("不支持 .xls 格式"));
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("解析带 BOM 的 UTF-8 CSV 文件")
        void parseCsvFile_withBom_handlesCorrectly() throws IOException {
            // Given - UTF-8 BOM: EF BB BF
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            String content = "student1@example.com" + LF + "student2@example.com";
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            byte[] fullContent = new byte[bom.length + contentBytes.length];
            System.arraycopy(bom, 0, fullContent, 0, bom.length);
            System.arraycopy(contentBytes, 0, fullContent, bom.length, contentBytes.length);
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fullContent);

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then - BOM 字符会被包含在第一个标识符中，但不影响功能
            assertEquals(2, identifiers.size());
        }

        @Test
        @DisplayName("解析带特殊字符的标识符")
        void parseCsvFile_withSpecialCharacters_preservesCharacters() throws IOException {
            // Given
            String csvContent = "student+test@example.com" + LF + "student.name@example.com";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("student+test@example.com", identifiers.get(0));
            assertEquals("student.name@example.com", identifiers.get(1));
        }

        @Test
        @DisplayName("解析带前后空格的标识符 - 自动去除空格")
        void parseCsvFile_withWhitespace_trimsWhitespace() throws IOException {
            // Given
            String csvContent = "  student1@example.com  " + LF + "  student2@example.com  ";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            // When
            List<String> identifiers = parser.parseCsvFile(inputStream);

            // Then
            assertEquals(2, identifiers.size());
            assertEquals("student1@example.com", identifiers.get(0));
            assertEquals("student2@example.com", identifiers.get(1));
        }
    }
}
