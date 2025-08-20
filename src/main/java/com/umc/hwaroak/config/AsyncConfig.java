package com.umc.hwaroak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

// 비동기 설정
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    @Bean("taskScheduler")
    public ThreadPoolTaskScheduler scheduledThreadPoolExecutor() {
        // Thread Pool 크기 5로 설정
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(5);

        // Shut down 후 작업에 대한 설정
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        executor.setRemoveOnCancelPolicy(true);
        executor.initialize();
        return executor;
    }
}
