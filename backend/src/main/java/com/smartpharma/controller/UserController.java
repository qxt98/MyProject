package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.User;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 系统用户管理接口 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OperationLogService operationLogService;

    @GetMapping
    public Result<List<User>> list() {
        List<User> list = userService.listAll();
        list.forEach(u -> u.setPassword(null));
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User u = userService.getById(id);
        u.setPassword(null);
        return Result.ok(u);
    }

    @PostMapping
    public Result<User> create(@RequestBody User u, HttpServletRequest request) {
        User saved = userService.create(u);
        saved.setPassword(null);
        operationLogService.record("系统管理", "新增用户", "用户 " + saved.getUsername(), request);
        return Result.ok(saved);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User u, HttpServletRequest request) {
        User updated = userService.update(id, u);
        updated.setPassword(null);
        operationLogService.record("系统管理", "修改用户", "用户ID:" + id, request);
        return Result.ok(updated);
    }

    /** 删除用户（需求 F-Sys-01 增删改查） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        userService.delete(id);
        operationLogService.record("系统管理", "删除用户", "用户ID:" + id, request);
        return Result.ok();
    }
}
