package top.icss.util;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Data;

import static top.icss.util.Constants.*;

/**
 * @author cd
 * @desc 协议消息工具类
 * @create 2020/5/9 17:13
 * @since 1.0.0
 */
@Data
public class ProtoUtil {
    private int code;
    private String body;

    public ProtoUtil(int code, String body) {
        this.code = code;
        this.body = body;
    }

    /**
     * pong
     * @return
     */
    public static String buildPongProto() {
        return buildProto(RES_PONG, null);
    }

    public static String buildProto(int code, String body){
        ProtoUtil proto = new ProtoUtil(code, body);
        return JSON.toJSONString(proto);
    }

    /**
     * 将对象转json
     * @param obj
     * @return
     */
    public static String objToString(Object obj){
        return JSON.toJSONString(obj);
    }

    /**
     * 消息响应
     * @param ctx
     * @param str
     * @return
     */
    public static void writeAndFlush(ChannelHandlerContext ctx, String str){
        ctx.writeAndFlush(new TextWebSocketFrame(str));
    }
}
