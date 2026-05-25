package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("fk_reim_allocation")
public class AllocationEntity {
    @TableId
    private String id;
    private String mainId;
    private Integer sortNo;
    private String allocCompanyId;
    private String allocCompanyNo;
    private String allocCompanyName;
    private String projectId;
    private String projectNo;
    private String projectName;
    private BigDecimal allocationRatio;
    private BigDecimal allocationAmount;
}
