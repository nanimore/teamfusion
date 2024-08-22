package com.zoro.teamfusion;

import com.alibaba.excel.EasyExcel;
import com.zoro.teamfusion.moder.domain.User;
import com.zoro.teamfusion.once.importuser.XingQiuTableUserInfo;
import com.zoro.teamfusion.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 导入用户测试
 *
 */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("ZORO");
            user.setUserAccount("ZORO");
            user.setAvatarUrl("http://p0.itc.cn/images01/20200529/6ec722cce0ba4c519b69db02be956b35.jpeg");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456");
            user.setEmail("123456@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            userList.add(user);
        }
        // 20 秒 10 万条
        //65915ms
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }


    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 读Excel
        String fileName = "D:\\Projects\\IDEA\\teamfusion\\src\\main\\resources\\prodExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<User> userInfoList =
                EasyExcel.read(fileName).head(User.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());

        // 分十组
        int batchSize = 1000;

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < userInfoList.size(); i+=batchSize) {
            List<User> userList = userInfoList.subList(i, Math.min(userInfoList.size(), i+batchSize));
            userList.forEach(user -> user.setId(null));
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        // 26.8925107秒 10 万条
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds() + "s");
    }
}
