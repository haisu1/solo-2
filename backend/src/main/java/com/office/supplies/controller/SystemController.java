package com.office.supplies.controller;

import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.Result;
import com.office.supplies.entity.User;
import com.office.supplies.service.DepartmentService;
import com.office.supplies.service.RoleService;
import com.office.supplies.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/users")
    public Result<PageResult<User>> getUserList(@Valid PageQuery query) {
        return Result.success(userService.getUserPage(query));
    }

    @GetMapping("/users/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userService.getUserDetail(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping("/users")
    public Result<Void> createUser(@RequestBody User user) {
        try {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456");
            }
            userService.saveUser(user);
            return Result.success("创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        try {
            userService.saveUser(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/roles")
    public Result<List<?>> getRoleList() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/departments")
    public Result<List<?>> getDepartmentList() {
        return Result.success(departmentService.getAllDepartments());
    }
}
