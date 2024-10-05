package com.shijiawei.secretblog.user.enumValue;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: Gender
 * Description:
 *
 * @Create 2024/9/18 下午5:44
 */
@Getter
@AllArgsConstructor
public enum Role {
    ADMIN(0,"管理員"),
    NORMALUSER(1,"普通用戶");

    @EnumValue
    private final int value;

    @JsonValue
    private final String code;

}
