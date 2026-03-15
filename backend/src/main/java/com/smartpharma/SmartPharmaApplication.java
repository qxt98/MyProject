package com.smartpharma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 药品信息智能化管理系统 - 启动类
 * Spring Boot 应用入口，扫描本包及子包下的组件、配置与接口。
 */
@SpringBootApplication
public class SmartPharmaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPharmaApplication.class, args);
    }
}
