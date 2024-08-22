package com.zoro.teamfusion;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;


class GetTest {

    @Test
    void readExcel(){


        String excelPath = "M:\\桌面\\桌面文件夹\\用户行为事件建模\\testdata1.1.xlsx";
        ExcelReader reader = ExcelUtil.getReader(new File(excelPath));
        //第一个参数：表示读取Excel表头的第2行获取字段名来映射到实体类中
        //第二个参数：代表从Excel第4行开始读取数据
        //第三个参数：HeroStarupAtb.class 是你自己对应的实体类
        long startTime = System.currentTimeMillis();
        List<Entity> events = null;

        for (int i = 0; i < 1000; i++) {
//            System.out.println("第" + i + "次循环开始……");
            events = reader.read(0, 1, Entity.class);
            events.get(0);
        }
        reader.close();
        long endTime = System.currentTimeMillis();
        System.out.println("定义在for循环外面耗时" + (endTime - startTime));

//        excelPath = "M:\\桌面\\桌面文件夹\\用户行为事件建模\\testdata1.1.xlsx";
//        reader = ExcelUtil.getReader(new File(excelPath));
//        long startTime1 = System.currentTimeMillis();
//        for (int i = 0; i < 10; i++) {
//            List<Entity> eventss = reader.read(0, 1, Entity.class);
//            eventss.get(0);
//        }
//        reader.close();
//        long endTime1 = System.currentTimeMillis();
//        System.out.println("定义在for循环里面耗时" + (endTime1 - startTime1));

    }

    @Test
    void readExcel1(){


        String excelPath = "M:\\桌面\\桌面文件夹\\用户行为事件建模\\testdata1.1.xlsx";
        ExcelReader reader = ExcelUtil.getReader(new File(excelPath));
        //第一个参数：表示读取Excel表头的第2行获取字段名来映射到实体类中
        //第二个参数：代表从Excel第4行开始读取数据
        //第三个参数：HeroStarupAtb.class 是你自己对应的实体类
//        long startTime = System.currentTimeMillis();
//        List<Entity> events = null;
//
//        for (int i = 0; i < 10; i++) {
//            events = reader.read(0, 1, Entity.class);
//            events.get(0);
//        }
//        reader.close();
//        long endTime = System.currentTimeMillis();
//        System.out.println("定义在for循环外面耗时" + (endTime - startTime));
//
//        excelPath = "M:\\桌面\\桌面文件夹\\用户行为事件建模\\testdata1.2.xlsx";
//        reader = ExcelUtil.getReader(new File(excelPath));
        long startTime1 = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            List<Entity> eventss = reader.read(0, 1, Entity.class);
            eventss.get(0);
        }
        reader.close();
        long endTime1 = System.currentTimeMillis();
        System.out.println("定义在for循环里面耗时" + (endTime1 - startTime1));

    }
}

@Data
class Entity {

    private Integer user_id;
    private String operation;
    private String component_element;
    private String page_data;
    private String input_content;
    private String fw_class;
    private String time;
    private Integer is_recommand;
    private String target;

}
