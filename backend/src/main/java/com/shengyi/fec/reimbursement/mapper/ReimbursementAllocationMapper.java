
package com.shengyi.fec.reimbursement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengyi.fec.reimbursement.entity.ReimbursementAllocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReimbursementAllocationMapper extends BaseMapper<ReimbursementAllocationEntity> {

    List<ReimbursementAllocationEntity> selectByMainId(@Param("mainId") Long mainId);

    void deleteByMainId(@Param("mainId") Long mainId);
}
