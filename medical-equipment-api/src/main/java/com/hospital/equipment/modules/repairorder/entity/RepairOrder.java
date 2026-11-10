package com.hospital.equipment.modules.repairorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报修工单实体
 */
@Data
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long deviceId;
    private Long reporterId;
    private Long assigneeId;
    private String description;
    private String images;        // 逗号分隔的图片URL
    private Integer status;       // 1待受理 2维修中 3待评价 4已完成 5已关闭
    private String repairRemark;
    private Integer rating;       // 1-5
    private String comment;
    private LocalDateTime finishTime;
    private String closeReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
