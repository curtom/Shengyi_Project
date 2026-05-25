package com.shengyi.fec.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "成功"),
    PARAM_INVALID(40001, "参数校验失败"),
    ITINERARY_DATE_DUP(40002, "人员与日期范围不可重复"),
    SUBSIDY_OVER_STANDARD(40003, "补助金额超出标准"),
    ALLOCATION_RATIO(40004, "分摊比例合计必须为100%"),
    ALLOCATION_AMOUNT(40005, "分摊金额合计必须等于补助总金额"),
    DATE_INVALID(40006, "到达日期不可早于出发日期，且不可晚于当前日期"),
    DB_ERROR(50001, "数据库操作异常");

    private final int code;
    private final String message;
}
