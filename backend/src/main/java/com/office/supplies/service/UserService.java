package com.office.supplies.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.JwtUtil;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.entity.Role;
import com.office.supplies.entity.User;
import com.office.supplies.mapper.RoleMapper;
import com.office.supplies.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        user = userMapper.getUserDetail(user.getId());
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), jwtSecret, jwtExpiration);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public User getUserDetail(Long id) {
        return userMapper.getUserDetail(id);
    }

    public Role getRoleById(Long roleId) {
        if (roleId == null) {
            return null;
        }
        return roleMapper.selectById(roleId);
    }

    public List<Long> getUserIdsByRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return userMapper.getUserIdsByRoleCode(roleCode);
    }

    public List<Long> getDepartmentLeaderIds() {
        return userMapper.getDepartmentLeaderIds();
    }

    public PageResult<User> getUserPage(PageQuery query) {
        List<User> list = userMapper.getUserList(query.getKeyword());
        long total = list.size();
        long start = (query.getCurrent() - 1) * query.getSize();
        long end = Math.min(start + query.getSize(), total);
        List<User> records = list.subList((int) start, (int) end);
        PageResult<User> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(query.getCurrent());
        result.setSize(query.getSize());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (user.getId() != null) {
            wrapper.ne(User::getId, user.getId());
        }
        Long count = this.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        return this.saveOrUpdate(user);
    }
}
