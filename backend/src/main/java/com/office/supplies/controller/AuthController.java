package com.office.supplies.controller;

import com.office.supplies.common.Result;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.User;
import com.office.supplies.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        try {
            Map<String, Object> data = userService.login(username, password);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        User user = userService.getUserDetail(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        UserContext.clear();
        return Result.successMsg("退出成功");
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("原密码和新密码不能为空");
        }
        Long userId = UserContext.getCurrentUserId();
        User user = userService.getById(userId);
        if (!oldPassword.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }
        user.setPassword(newPassword);
        userService.updateById(user);
        return Result.successMsg("密码修改成功");
    }
}
