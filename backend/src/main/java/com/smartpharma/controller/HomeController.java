package com.smartpharma.controller;

import com.smartpharma.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API 根路径：访问 /api 或 /api/ 时返回欢迎信息与可用接口说明，避免 404
 */
@RestController
@RequestMapping
public class HomeController {

    @GetMapping({"", "/"})
    public Result<Map<String, Object>> home() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("application", "药品信息智能化管理系统");
        data.put("message", "后端 API 已启动，请访问具体接口。当前 context-path 为 /api，以下路径均相对于 /api");
        data.put("swaggerUi", "/api/swagger-ui.html");
        data.put("openApiJson", "/api/v3/api-docs");
        data.put("endpoints", new String[]{
                "GET  /drugs           - 药品列表（分页）",
                "GET  /drugs/{id}      - 药品详情",
                "POST /drugs           - 新增药品",
                "PUT  /drugs/{id}      - 修改药品",
                "DELETE /drugs/{id}    - 删除药品",
                "POST /stock/in        - 入库",
                "POST /stock/out       - 出库",
                "GET  /stock/list      - 按药品查库存",
                "GET  /purchase        - 采购申请列表",
                "POST /auth/login      - 登录",
                "GET  /user            - 用户列表",
                "POST /guide/ask       - 用药指导提问"
        });
        return Result.ok(data);
    }
}
