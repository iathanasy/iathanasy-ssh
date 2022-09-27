package top.icss.xterm.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import top.icss.xterm.constants.XtermConstants;
import top.icss.xterm.service.XtermService;

/**
 * @author cd.wang
 * @create 2022-09-27 9:53
 */
@Component
public class XtermWebSocketHandler implements WebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(XtermWebSocketHandler.class);

    @Autowired
    private XtermService xtermService;

    /**
     * 用户连接上WebSocket的回调
     *
     * @param webSocketSession web socket session 对象
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        logger.info("用户:{},连接Xterm", webSocketSession.getAttributes().get(XtermConstants.USER_SESSION_ID));
        //调用初始化连接
        xtermService.initConnection(webSocketSession);
        logger.info("用户:{},初始化连接成功", webSocketSession.getAttributes().get(XtermConstants.USER_SESSION_ID));
    }

    /**
     * 收到消息前端的消息的处理
     *
     * @param webSocketSession web socket session 对象
     * @param webSocketMessage web socket 消息
     * @throws Exception 异常
     */
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        if (webSocketMessage instanceof TextMessage) {
            logger.info("用户:{},发送命令:{}", webSocketSession.getAttributes().get(XtermConstants.USER_SESSION_ID), ((TextMessage)webSocketMessage).getPayload());
            //调用service接收消息
            xtermService.recvHandle(((TextMessage) webSocketMessage).getPayload(), webSocketSession);
        } else if (webSocketMessage instanceof BinaryMessage) {
            logger.info("用户发送二进制消息");
        } else if (webSocketMessage instanceof PongMessage) {
            logger.info("用户发送PONG消息");
        } else {
            logger.error("Unexpected WebSocket message type: " + webSocketMessage);
        }
    }

    /**
     * 出现错误的回调
     *
     * @param webSocketSession web socket session 对象
     * @param throwable        错误信息对象
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        logger.error("数据传输错误");
    }

    /**
     * 连接关闭的回调
     *
     * @param webSocketSession web socket session 对象
     * @param closeStatus      关闭状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        logger.info("用户:{}断开Xterm连接", String.valueOf(webSocketSession.getAttributes().get(XtermConstants.USER_SESSION_ID)));
        //调用service关闭连接
        xtermService.close(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
