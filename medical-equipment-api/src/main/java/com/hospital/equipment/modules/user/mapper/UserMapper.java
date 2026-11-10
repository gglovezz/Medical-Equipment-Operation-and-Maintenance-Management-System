package com.hospital.equipment.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.equipment.modules.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}
