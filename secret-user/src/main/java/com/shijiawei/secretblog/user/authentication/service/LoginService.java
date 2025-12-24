package com.shijiawei.secretblog.user.authentication.service;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ClassName: LoginService
 * Description:
 *
 * @Create 2025/12/23 上午12:14
 */

public interface LoginService {

    R login(UmsUserLoginDTO umsUserLoginDTO, HttpServletResponse response);

}
