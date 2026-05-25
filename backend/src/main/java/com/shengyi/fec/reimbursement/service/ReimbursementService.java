
package com.shengyi.fec.reimbursement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shengyi.fec.reimbursement.dto.request.ReimbursementSaveRequest;
import com.shengyi.fec.reimbursement.dto.response.ReimbursementDetailResponse;
import com.shengyi.fec.reimbursement.dto.response.ReimbursementListResponse;
import com.shengyi.fec.reimbursement.entity.ReimbursementMainEntity;

public interface ReimbursementService {

    Long save(ReimbursementSaveRequest request);

    Long submit(Long id);

    ReimbursementDetailResponse getDetail(Long id);

    IPage<ReimbursementListResponse> getList(Integer pageNum, Integer pageSize, String keyword, Integer status, String applicant);
}
