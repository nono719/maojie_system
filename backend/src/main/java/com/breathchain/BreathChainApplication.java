package com.breathchain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.breathchain.mapper")
@EnableAsync
public class BreathChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(BreathChainApplication.class, args);
    }
}
