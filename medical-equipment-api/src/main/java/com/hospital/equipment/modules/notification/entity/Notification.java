package com.hospital.equipment.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;       // REPAIR_ACCEPTED/REPAIR_FINISHED/VERIFY_EXPIRE/INSPECT_ASSIGNED
    private Integer isRead;    // 0未读 1已读
    private Long refId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
