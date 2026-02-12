package com.microp.snowflake;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    Logger logger = LoggerFactory.getLogger(SnowflakeConfig.class);

    @Value("${WORKER_ID:default-worker-0}")
    private String workerIdWithName;

    @Value("${DATACENTER_ID:0}")
    private Long datacenterId;

    @Bean
    @ConditionalOnMissingBean(Snowflake.class)
    public Snowflake snowflake(){

        long workerId = Long.parseLong(workerIdWithName.split("-")[2]);

        logger.info("This project contains placeholder bean for snowflake instance, making your own highly recommended.");
        logger.info("SnowflakeConfig Using datacenterId={} workerId={}", datacenterId, workerId);
        return new Snowflake(workerId,datacenterId);
    }


}