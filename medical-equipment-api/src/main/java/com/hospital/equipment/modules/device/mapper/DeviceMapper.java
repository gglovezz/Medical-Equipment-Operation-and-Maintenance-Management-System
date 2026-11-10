package com.hospital.equipment.modules.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<MedicalDevice> {
}
