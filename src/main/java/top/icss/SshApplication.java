package top.icss;

import top.icss.netty.WebSocketServer;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 10:49
 * @since 1.0.0
 */
public class SshApplication {

    public static void main(String[] args) throws Exception {
        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.start();
       
    }
}
