package com.smartpharma.config;

import com.smartpharma.entity.User;
import com.smartpharma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 应用启动后检查并创建初始管理员（用户名 admin，密码 123456），若已存在则跳过。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class InitialAdminRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("系统管理员");
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("已创建初始管理员账号：admin / 123456");
    }
}
