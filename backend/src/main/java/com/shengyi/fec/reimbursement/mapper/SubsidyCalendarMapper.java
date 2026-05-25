
package com.shengyi.fec.reimbursement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengyi.fec.reimbursement.entity.SubsidyCalendarEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SubsidyCalendarMapper extends BaseMapper<SubsidyCalendarEntity> {

    BigDecimal selectEffectiveAmount(@Param("cityLevel") Integer cityLevel, @Param("targetDate") LocalDate targetDate);

    List<SubsidyCalendarEntity> selectByCityLevel(@Param("cityLevel") Integer cityLevel);
}
