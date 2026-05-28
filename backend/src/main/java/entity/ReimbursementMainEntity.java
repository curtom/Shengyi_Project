package entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("fk_reim_main")
public class ReimbursementMainEntity {
    @TableId
    private String id;
    private String creationTime;
    private String reimbursementNo;
    private String reimbursementTitle;
    private String reimburserId;
    private String reimburserNo;
    private String reimburserName;
    private String reimDepartmentId;
    private String reimDepartmentNo;
    private String reimDepartmentName;
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
    private String businessTripReason;
    private BigDecimal subsidyTotal;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private String remarks;
    private String docStatus;
    private String docDate;
}
