package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    @Select("SELECT * FROM biz_approval_node WHERE flow_id = #{flowId} ORDER BY node_level ASC")
    List<ApprovalNode> getNodesByFlowId(@Param("flowId") Long flowId);

    @Select("DELETE FROM biz_approval_node WHERE flow_id = #{flowId}")
    void deleteByFlowId(@Param("flowId") Long flowId);
}
