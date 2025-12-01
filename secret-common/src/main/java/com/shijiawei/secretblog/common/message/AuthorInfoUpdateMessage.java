package com.shijiawei.secretblog.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ClassName: AuthorInfoUpdateMessage
 * Description:
 *
 * @Create 2025/12/1 下午6:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorInfoUpdateMessage implements Serializable {

    private Long userId;
//    private String nickName;
    private String avatar;
    private Long timestamp;
}
