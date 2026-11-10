package com.hospital.equipment.modules.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医疗设备实体
 */
@Data
@TableName("medical_device")
public class MedicalDevice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceName;
    private String model;
    private String snCode;
    private String department;
    private String location;
    private String supplier;
    private LocalDate purchaseDate;
    private Integer status;       // 1正常 2维修中 3停用 4检定中
    private LocalDate lastInspectDate;
    private LocalDate nextInspectDate;
    private LocalDate verifyExpireDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
