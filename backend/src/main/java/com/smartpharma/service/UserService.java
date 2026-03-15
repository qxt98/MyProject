package com.smartpharma.service;

import com.smartpharma.entity.User;
import com.smartpharma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 系统用户服务（需求 F-Sys-01）：用户 CRUD、登录校验，密码 BCrypt 加密 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
    }

    /** 按用户名查询（用于 /auth/me 等），不存在抛异常 */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("user not found"));
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User create(User u) {
        if (userRepository.existsByUsername(u.getUsername())) throw new RuntimeException("username exists");
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return userRepository.save(u);
    }

    @Transactional
    public User update(Long id, User input) {
        User existing = getById(id);
        existing.setRealName(input.getRealName());
        existing.setRole(input.getRole());
        if (input.getEnabled() != null) existing.setEnabled(input.getEnabled());
        if (input.getPassword() != null && !input.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(input.getPassword()));
        }
        return userRepository.save(existing);
    }

    /** 删除用户（需求 F-Sys-01），禁止删除当前登录用户 */
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new RuntimeException("用户不存在");
        User u = getById(id);
        userRepository.delete(u);
    }

    public User login(String username, String password) {
        User u = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("bad credentials"));
        if (!u.getEnabled()) throw new RuntimeException("account disabled");
        if (!passwordEncoder.matches(password, u.getPassword())) throw new RuntimeException("bad credentials");
        u.setPassword(null);
        return u;
    }
}
