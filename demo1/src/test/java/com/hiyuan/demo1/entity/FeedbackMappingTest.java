package com.hiyuan.demo1.entity;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FeedbackMappingTest {

    @Test
    void contentFieldShouldMapToContentColumn() throws NoSuchFieldException {
        Field contentField = Feedback.class.getDeclaredField("content");
        Column column = contentField.getAnnotation(Column.class);

        assertNotNull(column, "content 字段必须有 @Column 注解");
        assertEquals("content", column.name(), "content 字段应映射到 t_feedback.content 列");
    }
}
