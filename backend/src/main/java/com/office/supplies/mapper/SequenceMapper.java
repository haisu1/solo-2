package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.Sequence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SequenceMapper extends BaseMapper<Sequence> {

    @Select("SELECT * FROM biz_sequence WHERE seq_name = #{seqName} AND seq_date = #{seqDate} FOR UPDATE")
    Sequence selectBySeqNameAndDateForUpdate(@Param("seqName") String seqName, @Param("seqDate") String seqDate);

    @Update("UPDATE biz_sequence SET current_value = current_value + 1, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version}")
    int incrementValue(@Param("id") Long id, @Param("version") Integer version);
}
