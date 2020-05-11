package top.icss.netty.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import top.icss.entity.Remote;
import top.icss.util.ProtoUtil;

import java.util.List;

import static top.icss.util.Constants.*;

/**
 * @author cd
 * @desc 命令处理handler
 * @create 2020/5/9 15:07
 * @since 1.0.0
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        //log.info("---------------WebSocketServerHandler-------------------");
        String message = frame.text();
        JSONObject json = JSONObject.parseObject(message);
        int code = json.getInteger("code");
        String response = "{}";
        switch(code){
            case REQ_PING:
                //log.info("ping");
                response = ProtoUtil.buildPongProto();
                break;
            case REQ_CMD:
                String body = json.getString("body");
                log.info("cmd---> {}",body);
                Remote remote = ctx.channel().attr(authAttr).get();
                if(remote != null){
                    List<String> list = null;
                    try {
                        list = remote.getSsh().exec(body);
                    } catch (Exception e) {
                        log.error("执行 exec 异常!");
                        remote.getSsh().connect(remote);
                    }
                    list.stream().forEach((res) ->{
                        ctx.write(new TextWebSocketFrame(ProtoUtil.buildProto(RES_CMD, res)));
                    });
                    //response = ProtoUtil.buildProto(RES_CMD, ProtoUtil.objToString(list));
                    ctx.flush();
                }

                break;
            case REQ_QUIT:
                //log.info("quit");
                response = ProtoUtil.buildProto(RES_QUIT, null);
                break;
            default:
                log.warn("The code [{}] can't be auth!!!", code);
                response = ProtoUtil.buildProto(RES_SYS, "请求错误！");
                break;
        }

        ProtoUtil.writeAndFlush(ctx, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("connection error and close the channel", cause);
    }

}
