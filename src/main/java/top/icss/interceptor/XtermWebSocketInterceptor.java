package top.icss.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import top.icss.xterm.constants.XtermConstants;

import java.util.Map;
import java.util.UUID;

/**
 * xterm 的拦截器
 * @author cd.wang
 * @create 2022-09-27 9:58
 */
@Component
public class XtermWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                   ServerHttpResponse serverHttpResponse,
                                   WebSocketHandler webSocketHandler,
                                   Map<String, Object> map) throws Exception {
        map.put(XtermConstants.USER_SESSION_ID, UUID.randomUUID().toString());
        return true;
    }


    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest,
                               ServerHttpResponse serverHttpResponse,
                               WebSocketHandler webSocketHandler,
                               Exception e) {

    }
}

