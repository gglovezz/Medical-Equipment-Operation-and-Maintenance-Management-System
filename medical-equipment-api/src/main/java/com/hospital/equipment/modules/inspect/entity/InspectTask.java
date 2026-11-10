package com.hospital.equipment.modules.inspect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检任务实体
 */
@Data
@TableName("inspect_task")
public class InspectTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private LocalDate taskDate;
    private Long inspectorId;
    private Integer status;    // 1待巡检 2已完成 3异常转报修
    private Integer result;    // 1正常 2异常
    private String remark;
    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
