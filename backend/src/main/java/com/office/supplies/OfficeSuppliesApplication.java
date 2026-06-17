package com.office.supplies;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.office.supplies.mapper")
public class OfficeSuppliesApplication {
    public static void main(String[] args) {
        SpringApplication.run(OfficeSuppliesApplication.class, args);
    }
}
