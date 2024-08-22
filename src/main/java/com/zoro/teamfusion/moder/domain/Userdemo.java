package com.zoro.teamfusion.moder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.stereotype.Repository;

@Data
@TableName( value = "user" )
@Repository
public class Userdemo {
    @TableId( type = IdType.NONE)
    private Long id;
    private String name;
    private Integer age;
    private String email;
}