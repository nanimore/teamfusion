package com.zoro.teamfusion.moder.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author ZORO
 */

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -4524068695684216542L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
