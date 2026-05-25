
package com.shengyi.fec.reimbursement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengyi.fec.reimbursement.entity.ReimbursementSubsidyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReimbursementSubsidyMapper extends BaseMapper<ReimbursementSubsidyEntity> {

    List<ReimbursementSubsidyEntity> selectByMainId(@Param("mainId") Long mainId);

    void deleteByMainId(@Param("mainId") Long mainId);
}
