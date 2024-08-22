package com.zoro.teamfusion.service;

import com.zoro.teamfusion.moder.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ZORO
 * @description 针对user用户表的数据库操作
 */
public interface UserService extends IService<User> {


    /**
     *用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param user 原始信息
     * @return 脱敏信息
     */
    User getSafetyUser(User user);

    /**\
     * 退出登录
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    int updateUser(User user, User loginUser);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

}
