package com.smartpharma.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger 配置
 * 配置文档标题、描述、版本；Swagger UI 与 JSON 文档地址随 context-path 自动为 /api 下。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("药品信息智能化管理系统 API")
                        .description("基于 Spring Boot 的后端接口文档，包含药品、库存、采购、处方、用药指导、用户与认证等模块。")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Pharma")
                                .url("")));
    }
}
