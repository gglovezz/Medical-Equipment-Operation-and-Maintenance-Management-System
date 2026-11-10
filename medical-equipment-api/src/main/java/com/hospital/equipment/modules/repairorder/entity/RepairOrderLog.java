package com.hospital.equipment.modules.repairorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工单操作日志实体
 */
@Data
@TableName("repair_order_log")
public class RepairOrderLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long operatorId;
    private String action;   // SUBMIT/ACCEPT/FINISH/RATE/CLOSE/ASSIGN
    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
