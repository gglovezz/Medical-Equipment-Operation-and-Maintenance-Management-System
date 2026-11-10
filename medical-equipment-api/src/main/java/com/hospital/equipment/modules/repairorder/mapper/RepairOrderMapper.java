package com.hospital.equipment.modules.repairorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.equipment.modules.repairorder.entity.RepairOrder;
import com.hospital.equipment.modules.repairorder.entity.RepairOrderLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {
}
