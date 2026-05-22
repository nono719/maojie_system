package com.breathchain.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.breathchain.entity.DoctorProfile;
import com.breathchain.entity.SysUser;
import com.breathchain.mapper.DoctorProfileMapper;
import com.breathchain.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 无需登录即可访问的公开接口。
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final SysUserMapper userMapper;
    private final DoctorProfileMapper doctorMapper;

    /** 注册页用：列出已通过认证的医生（供患者绑定 / 注册时挑选） */
    @GetMapping("/doctors")
    public List<Map<String, Object>> certifiedDoctors() {
        List<DoctorProfile> profiles = doctorMapper.selectList(
            Wrappers.<DoctorProfile>lambdaQuery().eq(DoctorProfile::getCertified, 1)
        );
        List<Map<String, Object>> out = new ArrayList<>();
        for (DoctorProfile p : profiles) {
            SysUser u = userMapper.selectById(p.getUserId());
            if (u == null) continue;
            Map<String, Object> row = new HashMap<>();
            row.put("userId", u.getId());
            row.put("username", u.getUsername());
            row.put("realName", u.getRealName());
            row.put("hospital", p.getHospital());
            row.put("department", p.getDepartment());
            row.put("title", p.getTitle());
            out.add(row);
        }
        return out;
    }
}
