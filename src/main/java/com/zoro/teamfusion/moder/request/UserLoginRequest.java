package com.zoro.teamfusion.moder.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author ZORO
 */

@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = 4454366983943412812L;
    private String userAccount;
    private String userPassword;

}
