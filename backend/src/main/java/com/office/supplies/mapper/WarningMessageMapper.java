package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.WarningMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WarningMessageMapper extends BaseMapper<WarningMessage> {

    @Select("<script>" +
            "SELECT w.* FROM biz_warning_message w " +
            "<where>" +
            "<if test='userId != null'>" +
            "AND w.user_id = #{userId}" +
            "</if>" +
            "<if test='readFlag != null'>" +
            "AND w.read_flag = #{readFlag}" +
            "</if>" +
            "<if test='warningLevel != null and warningLevel != \"\"'>" +
            "AND w.warning_level = #{warningLevel}" +
            "</if>" +
            "</where>" +
            "ORDER BY w.id DESC LIMIT 200" +
            "</script>")
    List<WarningMessage> getWarningMessages(@Param("userId") Long userId,
                                            @Param("readFlag") Integer readFlag,
                                            @Param("warningLevel") String warningLevel);

    @Update("UPDATE biz_warning_message SET read_flag = 1, read_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("<script>" +
            "UPDATE biz_warning_message SET read_flag = 1, read_time = CURRENT_TIMESTAMP " +
            "<where>" +
            "<if test='userId != null'>" +
            "AND user_id = #{userId}" +
            "</if>" +
            "AND read_flag = 0" +
            "</where>" +
            "</script>")
    int markAllAsRead(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT warning_level as warningLevel, COUNT(*) as count " +
            "FROM biz_warning_message " +
            "<where>" +
            "<if test='userId != null'>" +
            "AND user_id = #{userId}" +
            "</if>" +
            "AND read_flag = 0" +
            "</where>" +
            "GROUP BY warning_level" +
            "</script>")
    List<java.util.Map<String, Object>> getUnreadCountByLevel(@Param("userId") Long userId);
}
