package com.hospital.equipment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 医疗设备运维管理系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.hospital.equipment.modules.**.mapper")
@EnableScheduling
public class EquipmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(EquipmentApplication.class, args);
        System.out.println("\n========================================");
        System.out.println(" 医疗设备运维管理系统启动成功!");
        System.out.println(" 后端API:   http://localhost:8080/api/");
        System.out.println(" Swagger:   http://localhost:8080/doc.html");
        System.out.println(" H2控制台:  http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}
