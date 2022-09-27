package top.icss.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.HandshakeInterceptor;
import top.icss.interceptor.XtermWebSocketInterceptor;
import top.icss.wrapper.WebSocketHandlerRegister;
import top.icss.xterm.constants.XtermConstants;
import top.icss.xterm.handler.XtermWebSocketHandler;

/**
 * @author cd.wang
 * @create 2022-09-27 9:57
 */
@Configuration
public class XtermWebSocketConfiguration extends AbstractWebSocketConfigurer {

    @Autowired
    private XtermWebSocketHandler xtermWebSocketHandler;

    @Autowired
    private XtermWebSocketInterceptor xtermWebSocketInterceptor;


    @Override
    protected WebSocketHandlerRegister getRegisterWebSocket() {
        return new WebSocketHandlerRegister(xtermWebSocketHandler, new String[]{XtermConstants.XTERM_WEB_REQUEST_PATH});
    }

    @Override
    protected HandshakeInterceptor[] websocketInterceptors() {
        return new HandshakeInterceptor[]{xtermWebSocketInterceptor};
    }
}
