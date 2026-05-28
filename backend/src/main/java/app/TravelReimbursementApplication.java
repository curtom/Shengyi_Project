package app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"common", "config", "controller", "service"})
@MapperScan("mapper")
public class TravelReimbursementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelReimbursementApplication.class, args);
    }
}
