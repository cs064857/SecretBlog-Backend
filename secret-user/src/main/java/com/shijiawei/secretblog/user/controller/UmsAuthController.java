//package com.shijiawei.secretblog.user.controller;
//
//import com.shijiawei.secretblog.common.utils.R;
//import com.shijiawei.secretblog.user.dto.UmsUserLoginDTO;
//import com.shijiawei.secretblog.user.dto.UmsUserRegisterDTO;
//import com.shijiawei.secretblog.user.service.UmsAuthService;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/auth")
//public class UmsAuthController {
//    @PostMapping("/register")
//    public void userRegister(){
//
//    }
//
//    @PostMapping("/login")
//    public R userLogin(@Validated UmsUserLoginDTO umsAuthLoginDTO ){
//        UmsAuthService.userLogin(umsAuthLoginDTO);
//        return R.ok();
//    }
//
//    @PostMapping
//    public R userLogout(){
//        return R.ok();
//    }
//}