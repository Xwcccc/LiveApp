package com.zsxy.dto;

import lombok.Data;

/**
 * 省去一些敏感信息
 */
@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}
