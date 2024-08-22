package com.zoro.teamfusion.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求

 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 4486995033735722818L;

    private long id;
}
