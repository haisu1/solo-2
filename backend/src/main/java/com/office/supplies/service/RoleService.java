package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.entity.Role;
import com.office.supplies.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    public List<Role> getAllRoles() {
        return this.list();
    }
}
