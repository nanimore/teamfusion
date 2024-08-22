package com.zoro.teamfusion.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 5936907264534149345L;
    /**
     * 页面大小
     */
    private int pageSize;

    /**
     *当前是第几页
     */
    private int pageNum;
}
