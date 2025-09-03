package org.example.springboot.config;

import org.example.springboot.service.RecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private RecommendService recommendService;

    // 每天凌晨2点更新推荐
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleRecommendUpdate() {
        LOGGER.info("开始执行推荐更新任务");
        recommendService.updateRecommendations();
        LOGGER.info("推荐更新任务完成");
    }
} 