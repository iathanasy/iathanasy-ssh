package top.icss.config;

import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import top.icss.wrapper.WebSocketHandlerRegister;

/**
 * websocket抽象配置器
 * @author cd.wang
 * @create 2022-09-27 9:45
 */
public abstract class AbstractWebSocketConfigurer implements WebSocketConfigurer {

    /**
     * 获取注册的的websocket注册列表
     *
     * @return websocket的注册列表
     */
    protected abstract WebSocketHandlerRegister getRegisterWebSocket();

    /**
     * 注册webSocket的拦截器类别
     *
     * @return 得到拦截器列表
     */
    protected abstract HandshakeInterceptor[] websocketInterceptors();


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        WebSocketHandlerRegister registerWebSocket = getRegisterWebSocket();
        if (registerWebSocket == null) {
            return;
        }
        HandshakeInterceptor[] handshakeInterceptors = websocketInterceptors();
        if (handshakeInterceptors != null) {
            webSocketHandlerRegistry.addHandler(registerWebSocket.getWebSocketHandler(),
                    registerWebSocket.getRegisterPath()).addInterceptors(handshakeInterceptors).setAllowedOrigins("*");
        } else {
            webSocketHandlerRegistry.addHandler(registerWebSocket.getWebSocketHandler(),
                    registerWebSocket.getRegisterPath()).setAllowedOrigins("*");
        }
    }
}
