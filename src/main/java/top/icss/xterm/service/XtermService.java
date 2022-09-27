package top.icss.xterm.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Xterm ssh 接口
 * @author cd.wang
 * @create 2022-09-27 9:54
 */
public interface XtermService {

    /**
     * 初始化连接
     *
     * @param session websocket session对象
     */
    void initConnection(WebSocketSession session);

    /**
     * 处理客户端发的数据
     *
     * @param buffer  发送的数据
     * @param session websocket session对象
     */
    void recvHandle(String buffer, WebSocketSession session);

    /**
     * 数据写回前端 for websocket
     *
     * @param session websocket session对象
     * @param buffer  要发送的数据
     * @throws IOException IO 异常
     */
    void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    /**
     * 关闭连接
     *
     * @param session websocket session对象
     */
    void close(WebSocketSession session);
}
