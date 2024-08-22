package com.zoro.teamfusion;

import java.util.*;
import java.util.stream.Collectors;

class MapTest {
    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "a");
        map1.put("level", 3);
        map1.put("type", "3");
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "b");
        map2.put("level", 1);
        map2.put("type", "1");
        list.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("name", "c");
        map2.put("level", 1);
        map3.put("type", "1");
        list.add(map3);

        // 按照 level 值进行排序
        Collections.sort(list, Comparator.comparingInt(o -> (int) o.get("level")));

        // 按照 level 值分组
        Map<Integer, List<Map<String, Object>>> classified = list.stream()
                .collect(Collectors.groupingBy(o -> (int) o.get("level")));

        System.out.println(classified);

    }
}