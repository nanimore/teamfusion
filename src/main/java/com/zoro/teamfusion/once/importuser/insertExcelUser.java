package com.zoro.teamfusion.once.importuser;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.zoro.teamfusion.moder.domain.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入导出excel
 *

 */
public class insertExcelUser {

    public static void main(String[] args) {

        String fileName = "D:\\Projects\\IDEA\\teamfusion\\src\\main\\resources\\prodExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish

        List<User> users = createUsers(100000);

        // 写入Excel文件
        EasyExcel.write(fileName, User.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("Sheet1")
                .doWrite(users);

    }


    private static List<User> createUsers(int count) {
        List<User> userList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId((long) i);
            user.setUsername("User" + i);
            user.setUserAccount("account" + i);
            user.setProfile("我是一名对计算机科学充满热情的大学生。我热衷于深入研究[具体领域]，并完成了多个相关的课程项目，如[项目名称]，该项目旨在[项目简介]。通过这些项目，我不仅巩固了我的理论知识，还提升了在[编程语言，如Python、Java等]、[技术栈，如前端开发、数据库管理、机器学习框架等]方面的实践能力。");
            user.setAvatarUrl("http://p0.itc.cn/images01/20200529/6ec722cce0ba4c519b69db02be956b35.jpeg");
            user.setGender(1);
            user.setUserPassword("123456");
            user.setEmail("email" + i + "@example.com");
            user.setUserStatus(0);
            user.setPhone("1234567890" + (i % 10));
            user.setTags("['java','python','c','c++']");
            user.setUserRole(0);
            userList.add(user);
        }
        return userList;
    }
}
