package com.hospital.equipment.config;

import com.hospital.equipment.common.constant.DeviceStatus;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.mapper.DeviceMapper;
import com.hospital.equipment.modules.user.entity.SysUser;
import com.hospital.equipment.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 数据初始化 - 启动时自动插入测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final DeviceMapper deviceMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initDevices();
        log.info("========== 测试数据初始化完成 ==========");
        log.info("测试账号: admin(管理员) / engineer(工程师) / nurse(护士)");
        log.info("密码统一为: 123456");
    }

    private void initUsers() {
        if (userMapper.selectCount(null) > 0) return;

        insertUser("admin", "管理员", "admin", "设备科", "13800000000");
        insertUser("engineer", "李工", "engineer", "设备科", "13800000001");
        insertUser("nurse", "王护士", "nurse", "内科", "13800000002");
        insertUser("engineer2", "赵工", "engineer", "设备科", "13800000003");
        insertUser("nurse2", "张护士", "nurse", "外科", "13800000004");

        log.info("已初始化 {} 个用户", 5);
    }

    private void insertUser(String username, String realName, String role, String dept, String phone) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setRole(role);
        user.setDept(dept);
        user.setPhone(phone);
        user.setStatus(1);
        userMapper.insert(user);
    }

    private void initDevices() {
        if (deviceMapper.selectCount(null) > 0) return;

        LocalDate today = LocalDate.now();

        insertDevice("无创呼吸机", "SV-300", "WH20240001", "内科", "内科3病房",
                "迈瑞", today.minusYears(2), today.plusDays(30), today.plusYears(2));
        insertDevice("心电图机", "ECG-12", "EC20240005", "外科", "外科1病房",
                "GE医疗", today.minusYears(1), today.plusDays(15), today.plusYears(1));
        insertDevice("血压计", "BP-200", "BP20250010", "内科", "内科5病房",
                "鱼跃医疗", today.minusMonths(6), today.plusDays(45), today.plusYears(3));
        insertDevice("监护仪", "PM-8000", "PM20240012", "ICU", "ICU病房",
                "迈瑞医疗", today.minusYears(1), today.plusDays(20), today.plusYears(1));
        insertDevice("输液泵", "SP-200", "SP20230003", "儿科", "儿科2病房",
                "贝朗医疗", today.minusYears(3), today.plusDays(5), today.minusDays(25)); // 检定即将到期
        insertDevice("雾化器", "NB-150", "NB20250022", "呼吸科", "呼吸科治疗室",
                "欧姆龙", today.minusMonths(3), today.plusDays(60), today.plusYears(2));
        insertDevice("除颤仪", "Defi-Monitor", "DF20220008", "急诊科", "急诊抢救室",
                "飞利浦", today.minusYears(4), today, today.minusDays(3)); // 今天需要巡检
        insertDevice("注射泵", "SP-500", "SP20240045", "手术室", "手术室3",
                "史密斯医疗", today.minusYears(1), today.plusDays(10), today.plusYears(1));

        log.info("已初始化 {} 台设备", 8);
    }

    private void insertDevice(String name, String model, String snCode, String dept, String location,
                               String supplier, LocalDate purchaseDate, LocalDate nextInspectDate,
                               LocalDate verifyExpireDate) {
        MedicalDevice device = new MedicalDevice();
        device.setDeviceName(name);
        device.setModel(model);
        device.setSnCode(snCode);
        device.setDepartment(dept);
        device.setLocation(location);
        device.setSupplier(supplier);
        device.setPurchaseDate(purchaseDate);
        device.setStatus(DeviceStatus.NORMAL.getCode());
        device.setLastInspectDate(LocalDate.now().minusDays(30));
        device.setNextInspectDate(nextInspectDate);
        device.setVerifyExpireDate(verifyExpireDate);
        deviceMapper.insert(device);
    }
}
