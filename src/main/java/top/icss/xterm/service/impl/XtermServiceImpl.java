package top.icss.xterm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import top.icss.xterm.constants.JSchConnectInfo;
import top.icss.xterm.constants.XtermConstants;
import top.icss.xterm.entity.XtermCommandData;
import top.icss.xterm.service.XtermService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://www.cnblogs.com/goloving/p/15025262.html
 * Xterm实现类
 * @author cd.wang
 * @create 2022-09-27 9:55
 */
@Service
public class XtermServiceImpl implements XtermService, SmartInitializingSingleton {
    private final Logger logger = LoggerFactory.getLogger(XtermServiceImpl.class);
    /**
     * 存放ssh连接信息的map
     */
    private final Map<String, JSchConnectInfo> xtermConnectInfoMap = new ConcurrentHashMap<>(16);

    private static String  DEFAULT_CHART="UTF-8";
    private ThreadPoolExecutor workerExecutor;

    /**
     * 初始化连接
     *
     * @param session websocket session对象
     */
    @Override
    public void initConnection(WebSocketSession session) {
        JSch jSch = new JSch();
        JSchConnectInfo xtermConnectInfo = new JSchConnectInfo();
        xtermConnectInfo.setRemoteSsh(jSch);
        xtermConnectInfo.setWebSocketSession(session);
        String sessionId = String.valueOf(session.getAttributes().get(XtermConstants.USER_SESSION_ID));
        //将这个ssh连接信息放入map中
        xtermConnectInfoMap.put(sessionId, xtermConnectInfo);
    }

    /**
     * 处理前端发过来的数据请求
     *
     * @param buffer  发送的数据
     * @param session websocket session对象
     */
    @Override
    public void recvHandle(String buffer, WebSocketSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        XtermCommandData commandData;
        try {
            commandData = objectMapper.readValue(buffer, XtermCommandData.class);
        } catch (IOException e) {
            logger.error("Json转换异常");
            logger.error("异常信息:{}", e.getMessage());
            //这里返回给前端 线程池提交异常
            //todo
            return;
        }
        String userId = String.valueOf(session.getAttributes().get(XtermConstants.USER_SESSION_ID));
        if (XtermConstants.XTERM_OPERATE_CONNECT.equals(commandData.getOperate())) {
            //找到刚才存储的ssh连接对象
            JSchConnectInfo sshConnectInfo = xtermConnectInfoMap.get(userId);
            //启动线程异步处理
            try {
                workerExecutor.execute(() -> {
                    try {
                        connectToSsh(sshConnectInfo, commandData, session);
                    } catch (JSchException | IOException e) {
                        logger.error("异常信息:{}", e.getMessage());
                        close(session);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                //这里返回给前端 线程池提交异常
                //todo
            }
        } else if (XtermConstants.XTERM_OPERATE_COMMAND.equals(commandData.getOperate())) {
            String command = commandData.getCommand();
            JSchConnectInfo sshConnectInfo = xtermConnectInfoMap.get(userId);
            if (sshConnectInfo != null) {
                try {
                    transToSsh(sshConnectInfo.getChannel(), command);
                } catch (IOException e) {
                    logger.error("异常信息:{}", e.getMessage());
                    close(session);
                }
            }
        } else {
            logger.error("不支持的操作");
            close(session);
        }
    }

    /**
     * 返回数据给前端
     *
     * @param session websocket session对象
     * @param buffer  要发送的数据
     * @throws IOException IO 异常
     */
    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    /**
     * 关闭连接
     *
     * @param session websocket session对象
     */
    @Override
    public void close(WebSocketSession session) {
        String userId = String.valueOf(session.getAttributes().get(XtermConstants.USER_SESSION_ID));
        JSchConnectInfo sshConnectInfo = xtermConnectInfoMap.get(userId);
        if (sshConnectInfo != null) {
            //断开连接
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            //map中移除
            xtermConnectInfoMap.remove(userId);
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        BlockingQueue<Runnable> workerQueue = new LinkedBlockingQueue<>(4);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int coreSize = availableProcessors <= 1 ? 1 : availableProcessors >> 2;
        workerExecutor = new ThreadPoolExecutor(coreSize, coreSize, 30, TimeUnit.SECONDS, workerQueue,
                new ThreadFactory() {
                    private final AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, String.format("Xterm-remote-thread-%d", this.threadIndex.incrementAndGet()));
                    }
                }, (r, executor) -> {
            throw new RejectedExecutionException("请求太多，本次请求被拒绝，请稍后在重试，可适当关闭无操作的窗口");
        });
    }

    /**
     * 使用jsch连接终端
     *
     * @param sshConnectInfo   远程连接对象
     * @param commandData      命令数据对象
     * @param webSocketSession web socket 对象
     * @throws JSchException ssh异常
     * @throws IOException   io 异常
     */
    private void connectToSsh(JSchConnectInfo sshConnectInfo, XtermCommandData commandData, WebSocketSession webSocketSession) throws JSchException, IOException {
        Session session = null;
        Properties config = new Properties();
        // SSH 公钥检查机制 no、ask、yes
        config.put("StrictHostKeyChecking", "no");
        //获取jsch的会话
        session = sshConnectInfo.getRemoteSsh().getSession(commandData.getUsername(), commandData.getHost(), commandData.getPort());
        session.setConfig(config);
        //设置密码
        session.setPassword(commandData.getPassword());
        //添加私钥
//        sshConnectInfo.getRemoteSsh().addIdentity(priKeyBasePath);
        //连接  超时时间30s
        session.connect(30000);

        //开启shell通道
        Channel channel = session.openChannel("shell");

        //通道连接 超时时间3s
        channel.connect(3000);

        //设置channel
        sshConnectInfo.setChannel(channel);

        //转发消息
        transToSsh(channel, "\r");

        //读取终端返回的信息流
        try (InputStream inputStream = channel.getInputStream()) {
            //循环读取
            byte[] buffer = new byte[1024];
            int i = 0;
            //如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                System.err.println("available: " + inputStream.available() +" i: " + i);
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }
        } finally {
            //断开连接后关闭会话
            session.disconnect();
            channel.disconnect();
        }

    }

    /**
     * 将消息转发到终端
     *
     * @param channel ssh 通道
     * @param command 命令
     * @throws IOException io 异常
     */
    private void transToSsh(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }

}

