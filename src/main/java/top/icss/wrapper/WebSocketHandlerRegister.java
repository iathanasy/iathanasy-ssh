
package top.icss.wrapper;

import org.springframework.web.socket.WebSocketHandler;

/**
 * WebSocket注册对象包装器
 * @author cd.wang
 * @create 2022-09-27 9:45
 */
public class WebSocketHandlerRegister {

    private final WebSocketHandler webSocketHandler;

    private final String []registerPath;

    public WebSocketHandlerRegister(WebSocketHandler webSocketHandler, String []registerPath) {
        this.webSocketHandler = webSocketHandler;
        this.registerPath = registerPath;
    }

    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    public String[] getRegisterPath() {
        return registerPath;
    }
}
