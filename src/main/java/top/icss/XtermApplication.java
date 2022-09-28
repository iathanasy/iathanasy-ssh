package top.icss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 10:49
 * @since 1.0.0
 */
@EnableWebSocket
@SpringBootApplication
public class XtermApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(XtermApplication.class, args);
    }
}
/**
 * 链接
 {
 "operate": "connect",
 "host": "ip",
 "port": "22",
 "username": "用户名",
 "password": "密码"
 }

 * 指令
 {
 "operate": "command",
 "command": "pwd"
 }
 {
 "operate": "command",
 "command": "\r"
 }
 */
