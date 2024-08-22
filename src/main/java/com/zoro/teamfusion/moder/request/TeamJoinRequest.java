package com.zoro.teamfusion.moder.request;

import lombok.Data;
import java.io.Serializable;

/**
 * 加入队伍请求体
 */

@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -3350429575999064206L;
    private long teamId;

    private String password;
}
