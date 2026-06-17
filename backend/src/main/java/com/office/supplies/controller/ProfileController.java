package com.office.supplies.controller;

import com.office.supplies.common.Result;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.User;
import com.office.supplies.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Resource
    private UserService userService;

    @GetMapping
    public Result<User> getProfile() {
        Long userId = UserContext.getCurrentUserId();
        User user = userService.getUserDetail(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping
    public Result<Void> updateProfile(@RequestBody User user) {
        Long userId = UserContext.getCurrentUserId();
        user.setId(userId);
        user.setPassword(null);
        user.setRoleId(null);
        user.setStatus(null);
        userService.updateById(user);
        return Result.success("更新成功");
    }
}
