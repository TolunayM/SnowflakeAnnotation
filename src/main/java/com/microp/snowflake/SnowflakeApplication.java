package com.microp.snowflake;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SnowflakeApplication {




    public static void main(String[] args) {
        SpringApplication.run(SnowflakeApplication.class, args);
    }
}
