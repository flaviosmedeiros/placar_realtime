package br.com.solides.placar.consumer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class AsyncConfigTest {

    private final AsyncConfig config = new AsyncConfig();

    @Test
    void shouldCreateSseTaskExecutorWithExpectedThreadPoolSettings() {
        Executor executor = config.sseTaskExecutor();

        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) executor;
        try {
            assertEquals("sse-dispatch-", threadPool.getThreadNamePrefix());
            assertEquals(2, threadPool.getCorePoolSize());
            assertEquals(8, threadPool.getMaxPoolSize());
            assertEquals(1000, threadPool.getQueueCapacity());
            assertNotNull(threadPool.getThreadPoolExecutor().getRejectedExecutionHandler());
        } finally {
            threadPool.shutdown();
        }
    }
}
