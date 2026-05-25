
package com.shengyi.fec.reimbursement.common;

public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "数据不存在"),
    SERVER_ERROR(500, "服务器内部错误"),
    VALIDATION_ERROR(4001, "校验失败"),
    REIMB_NO_EXISTS(4002, "报销单号已存在"),
    ITINERARY_NOT_FOUND(4003, "行程不存在"),
    SUBMIT_FAILED(4004, "提交失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
