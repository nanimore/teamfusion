package com.zoro.teamfusion.once.importuser;

import com.alibaba.excel.EasyExcel;
import com.zoro.teamfusion.moder.domain.User;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入导出excel
 *

 */
public class ImportExcelUser {

    public static void main(String[] args) {

        String fileName = "D:\\Projects\\IDEA\\teamfusion\\src\\main\\resources\\prodExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<User> userInfoList =
                EasyExcel.read(fileName).head(User.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());
        Map<String, List<User>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(User::getUsername));
        for (Map.Entry<String, List<User>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {
                System.out.println("username = " + stringListEntry.getKey());
                System.out.println("1");
            }
        }
        System.out.println("不重复昵称数 = " + listMap.keySet().size());

    }
}
