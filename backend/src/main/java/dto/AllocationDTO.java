package dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllocationDTO {
    private String id;
    private String companyId;
    private String companyNo;
    private String companyName;
    private String projectId;
    private String projectNo;
    private String projectName;
    private BigDecimal ratio;
    private BigDecimal amount;
}