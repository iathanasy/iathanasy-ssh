package top.icss.util;

import io.netty.util.AttributeKey;
import top.icss.entity.Remote;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 17:09
 * @since 1.0.0
 */
public class Constants {
    //用户连接ssh
    public final static AttributeKey<Remote> authAttr = AttributeKey.newInstance("Auth");

    //系统消息
    public final static int RES_SYS = 10000;

    // 用户消息
    public final static int REQ_PING = 10010;
    public final static int RES_PONG = 10011;

    public final static int REQ_AUTH = 10012;
    public final static int RES_AUTH = 10013;

    public final static int REQ_CMD = 10014;
    public final static int RES_CMD = 10015;

    public final static int REQ_QUIT = 10016;
    public final static int RES_QUIT = 10017;

}
