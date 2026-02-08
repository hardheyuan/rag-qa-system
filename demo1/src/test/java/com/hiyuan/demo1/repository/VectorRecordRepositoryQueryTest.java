package com.hiyuan.demo1.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorRecordRepositoryQueryTest {

    @Test
    void findNearestVectorIdsUsesCosineOperator() throws NoSuchMethodException {
        Method method = VectorRecordRepository.class.getMethod(
                "findNearestVectorIds",
                String.class,
                UUID.class,
                int.class
        );

        Query query = method.getAnnotation(Query.class);
        assertNotNull(query, "findNearestVectorIds must keep @Query annotation");
        assertTrue(query.value().contains("<=>"), "findNearestVectorIds should use cosine operator <=>");
        assertFalse(query.value().contains("<->"), "findNearestVectorIds should not use L2 operator <->");
    }

    @Test
    void findNearestVectorIdsByOwnerIdsUsesCosineOperator() throws NoSuchMethodException {
        Method method = VectorRecordRepository.class.getMethod(
                "findNearestVectorIdsByOwnerIds",
                String.class,
                java.util.List.class,
                int.class
        );

        Query query = method.getAnnotation(Query.class);
        assertNotNull(query, "findNearestVectorIdsByOwnerIds must keep @Query annotation");
        assertTrue(query.value().contains("<=>"), "findNearestVectorIdsByOwnerIds should use cosine operator <=>");
        assertFalse(query.value().contains("<->"), "findNearestVectorIdsByOwnerIds should not use L2 operator <->");
    }
}
