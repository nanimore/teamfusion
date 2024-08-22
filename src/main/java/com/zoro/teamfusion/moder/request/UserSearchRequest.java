package com.zoro.teamfusion.moder.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author ZORO
 */

@Data
public class UserSearchRequest implements Serializable {


    private static final long serialVersionUID = -7476867471852089876L;
    private long userId;

}
