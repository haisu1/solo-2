package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.*, r.role_name, r.role_code, d.dept_name as department_name " +
            "FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "WHERE u.id = #{id}")
    User getUserDetail(@Param("id") Long id);

    @Select("SELECT u.*, r.role_name, r.role_code, d.dept_name as department_name " +
            "FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "WHERE u.username = #{username}")
    User getUserByUsername(@Param("username") String username);

    @Select("<script>" +
            "SELECT u.*, r.role_name, r.role_code, d.dept_name as department_name " +
            "FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.username LIKE CONCAT('%',#{keyword},'%') OR u.real_name LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY u.id DESC" +
            "</script>")
    List<User> getUserList(@Param("keyword") String keyword);

    @Select("SELECT u.id FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id WHERE r.role_code = #{roleCode}")
    List<Long> getUserIdsByRoleCode(@Param("roleCode") String roleCode);

    @Select("SELECT u.id FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id WHERE r.role_code IN ('DEPT_MANAGER','ADMIN')")
    List<Long> getDepartmentLeaderIds();
}
