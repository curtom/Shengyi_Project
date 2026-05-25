package com.shengyi.fec.reimbursement.controller;

import com.shengyi.fec.reimbursement.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public Result<String> healthCheck() {
        return Result.success("Server is running!");
    }
}

@RestController
@RequestMapping("/")
class RootController {
    
    @GetMapping("")
    public Result<String> root() {
        return Result.success("报销管理系统API服务运行中");
    }
}
