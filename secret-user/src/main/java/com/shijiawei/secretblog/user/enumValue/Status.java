package com.shijiawei.secretblog.user.enumValue;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: Status
 * Description:
 *
 * @Create 2024/9/18 下午10:31
 */
@AllArgsConstructor
@Getter
public enum Status {
    NORMAL(0,"Normal"),
    BAN(1,"Ban");

    @EnumValue
    private final int code;

    @JsonValue
    private final String name;
}
