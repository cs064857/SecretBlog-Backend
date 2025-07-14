//package com.shijiawei.secretblog.user.service.Impl;
//
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.shijiawei.secretblog.user.entity.UmsRole;
//import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
//import com.shijiawei.secretblog.user.service.UmsAuthService;
//import com.shijiawei.secretblog.user.service.UmsUserService;
//
//import lombok.extern.slf4j.Slf4j;
//
///**
// * ClassName: UmsAuthServiceImpl
// * Description: 認證服務實現類
// *
// * @Create 2025/1/24 上午12:30
// */
//@Service
//@Slf4j
//public class UmsAuthServiceImpl extends ServiceImpl<UmsUserMapper, UmsRole> implements Ums {
//
////    @Autowired
////    private UmsUserService umsUserService;
////
////    @Autowired
////    private BCryptPasswordEncoder passwordEncoder;
////
////    @Override
////    public void register(UmsUserRegisterDTO registerDTO) {
////        // TODO: 實現註冊邏輯
////        log.info("用戶註冊: {}", registerDTO);
////    }
////
////    @Override
////    public String login(String username, String password) {
////        // TODO: 實現登錄邏輯
////        log.info("用戶登錄: username={}", username);
////        return "token";
////    }
////
////    @Override
////    public void logout() {
////        // TODO: 實現登出邏輯
////        log.info("用戶登出");
////    }
//}
