package com.shijiawei.secretblog.user.enumValue;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
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
public enum Gender {
    MALE(1,"male"),
    FEMALE(2,"female"),
    OTHER(3,"other");

    @EnumValue
    private final int value;

    @JsonValue
    private final String code;

}
