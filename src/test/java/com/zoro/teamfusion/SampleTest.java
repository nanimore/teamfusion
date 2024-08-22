package com.zoro.teamfusion;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zoro.teamfusion.mapper.UserMapper;
import com.zoro.teamfusion.mapper.UserdomeMapper;
import com.zoro.teamfusion.moder.domain.User;
import com.zoro.teamfusion.moder.domain.Userdemo;
import com.zoro.teamfusion.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SampleTest {

    @Resource
    private UserdomeMapper userdomeMapper;
    @Resource
    Userdemo userdemo;
    @Resource
    private UserMapper userMapper;


    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userdemoList = userMapper.selectList(null);
        Assert.assertEquals(4, userdemoList.size());
        userdemoList.forEach(System.out::println);
    }
    @Test
    public void testDeleteById() {
        System.out.println(("----- Delete method test ------"));
        int result = userdomeMapper.deleteById(5L);
        System.out.println(result);

    }

    @Test
    public void testDelete() {
        System.out.println(("----- Delete method test ------"));
//        Map<String, Object> map = new HashMap<>();
//        map.put("age", 18);
        int result = userdomeMapper.delete(new QueryWrapper<Userdemo>().eq("name","zoro"));

        System.out.println(result);

    }

    @Test
    public void testInsert() {
        System.out.println(("----- testInsert method test ------"));
        userdemo.setId(6L);
        userdemo.setName("zoro");
        userdemo.setAge(23);
        userdemo.setEmail("sum@12324.com");
        int result = userdomeMapper.insert(userdemo);
        System.out.println(result);

    }

    @Test
    public void testUpdateById() {
        System.out.println(("----- testDeleteById method test ------"));
        userdemo.setId(2L);
        userdemo.setName("JackMa");
        userdemo.setAge(33);
//        user.setEmail("@12324.com");
        int result = userdomeMapper.updateById(userdemo);
        System.out.println(result);
    }

    @Test
    public void testUpdate() {
        System.out.println(("----- testDeleteById method test ------"));
        userdemo.setId(2L);
        userdemo.setName("JackMa");
        userdemo.setAge(55);
//        user.setEmail("@12324.com");
        int result = userdomeMapper.update(userdemo, new QueryWrapper<Userdemo>().eq("id",2));
        System.out.println(result);
    }

    @Test
    public void testSelectById() {
        System.out.println("------ testSelectById method test --------");
        Userdemo userdemo1 = userdomeMapper.selectById(2L);
        System.out.println(userdemo1);

    }

    @Test
    public void testSelectOne() {
        System.out.println("------ testSelectOne method test --------");
        Userdemo userdemo1 = userdomeMapper.selectOne(new QueryWrapper<Userdemo>().eq("age",28));
        System.out.println(userdemo1);

    }

    @Test
    public void testSelectBatchIds() {
        System.out.println("------ testSelectBatchIds method test --------");
//        List<Long> ids = ArrayList(2L,3L,4L);
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        List<Userdemo> userdemo1 = userdomeMapper.selectBatchIds(ids);
        System.out.println(userdemo1);
    }

    @Test
    public void testSelectByMap() {
        System.out.println("------ testSelectByMap method test --------");
        Map<String,Object> map = new HashMap<>();
        map.put("id",2L);
        map.put("age",55);
        List<Userdemo> userdemoList = userdomeMapper.selectByMap(map);
        System.out.println(userdemoList);
    }

    @Test
    public void testSelectCount() {
        System.out.println("------ testSelectCount method test --------");
        long count = userdomeMapper.selectCount(new QueryWrapper<Userdemo>().eq("age",28));
        System.out.println(count);

    }

    @Test
    public void testSelectList() {
        System.out.println("------ testSelectCount method test --------");

        List<Userdemo> userdemos = userdomeMapper.selectList(null);

        System.out.println(userdemos);
    }

    @Test
    public void testSelectPage() {
        System.out.println("------ testSelectPage method test --------");

        Page<Userdemo> page = new Page<>(1, 10);
        IPage<Userdemo> userPage =  userdomeMapper.selectPage(page, new QueryWrapper<Userdemo>().ge("age", 18));
        List<Userdemo> userdemos = page.getRecords();

        System.out.println(userPage);
        System.out.println(userdemos);
    }

    @Test
    public void testSelectCondition() {

        System.out.println("------ testSelectCondition method test --------");
        QueryWrapper<Userdemo> wrapper = new QueryWrapper<>();
        wrapper.eq(true,"id",2L);
        wrapper.eq(false,"age",28);
        List<Userdemo> userdemoList = userdomeMapper.selectList(wrapper);
        System.out.println(wrapper.toString());
        System.out.println(wrapper.getSqlSelect());
        System.out.println(userdemoList);
    }
    @Resource
    private UserService userService;

    @Test
    public void testLike(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("userAccount", "zoro");
        List<User> list = userService.list(wrapper);
        System.out.println(list);


    }

}