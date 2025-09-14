package com.example.redis_practice;

import com.example.redis_practice.entity.Product;
import com.example.redis_practice.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DistributedLockTests {

    @Autowired private ProductService productService;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(20);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    @DisplayName("분산 락 없이 동시 실행 - 동시성 문제 발생")
    void testWithoutDistributedLock() throws InterruptedException {
        // given
        Product product = new Product("운동화", 20);
        productService.addProduct(product);

        int threadCount = 20;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when: 20개의 스레드가 동시에 상품 재고를 1씩 감소
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStockWithoutDistributedLock(product.getId());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await(5, TimeUnit.SECONDS);

        // then: 동시성 문제로 인해 0이 아닌 값이 나올 가능성이 높음
        Product result = productService.getProduct(product.getId());
        assertNotEquals(0, result.getStock());
        System.out.println("락 없이 실행한 결과: " + result.getStock());
    }

    @Test
    @DisplayName("분산 락 적용 - 동시성 문제 해결")
    void testWithDistributedLock() throws InterruptedException {
        // given
        Product product = new Product("모자", 20);
        productService.addProduct(product);

        int threadCount = 20;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStockWithDistributedLock(product.getId());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await(10, TimeUnit.SECONDS);

        // then
        Product result = productService.getProduct(product.getId());
        assertEquals(0, result.getStock());
        System.out.println("락 적용한 결과: " + result.getStock());
    }
}
