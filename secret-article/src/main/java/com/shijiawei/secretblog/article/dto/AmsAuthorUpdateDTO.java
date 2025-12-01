package com.shijiawei.secretblog.article.dto;

import lombok.Data;

@Data
public class AmsAuthorUpdateDTO {
    private Long userId;
    private String nickName;
    private String avatar;
}
