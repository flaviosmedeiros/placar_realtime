package br.com.solides.placar.consumer.config;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "sseTaskExecutor")
    Executor sseTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("sse-dispatch-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000);
        executor.setRejectedExecutionHandler(
                (r, executor1) -> logger
                        .warn("Tarefa rejeitada, o pool de threads está cheio e a fila também está cheia"));
        executor.initialize();
        return executor;
    }
}