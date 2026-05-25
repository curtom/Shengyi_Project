package com.shengyi.fec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.shengyi.fec.reimbursement.mapper")
public class TravelReimbursementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelReimbursementApplication.class, args);
    }
}
