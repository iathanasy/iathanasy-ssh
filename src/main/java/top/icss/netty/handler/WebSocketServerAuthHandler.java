package top.icss.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import top.icss.entity.Remote;
import top.icss.ssh.SshServer;
import top.icss.ssh.impl.SshServerImpl;
import top.icss.util.ProtoUtil;

import java.util.HashMap;
import java.util.Map;

import static top.icss.util.Constants.*;

/**
 * @author cd
 * @desc 授权登录handler
 * @create 2020/5/9 16:59
 * @since 1.0.0
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketServerAuthHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        //log.info("---------------WebSocketServerAuthHandler-------------------");
        //处理授权登录
        String message = frame.text();
        JSONObject json = JSONObject.parseObject(message);
        int code = json.getInteger("code");
        if(code == REQ_AUTH){
            //授权登录
            String body = json.getString("body");
            Remote remote = JSON.parseObject(body, Remote.class);
            log.info("remote--> {}", remote);
            remote = getRemote(remote);
            //初始化
            SshServer ssh = new SshServerImpl();
            //连接
            ssh.connect(remote);

            remote.setSsh(ssh);
            Map<String, Object> map = new HashMap<String, Object>(2);
            if(remote.getSsh() != null) {
                map.put("auth", true);
                map.put("msg", "success");
                //设置附件(寄生虫) 寄付在每条channel上
                ctx.channel().attr(authAttr).set(remote);
                //登录之后移除handler
                ctx.pipeline().remove(this);
            }else{
                map.put("auth",false);
                map.put("msg","error");
            }
            String authProto = ProtoUtil.buildProto(RES_AUTH, ProtoUtil.objToString(map));
            ProtoUtil.writeAndFlush(ctx, authProto);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof Exception){
            Map<String,Object> map = new HashMap<String,Object>(2);
            map.put("auth",false);
            map.put("msg","error");
            String authProto = ProtoUtil.buildProto(RES_AUTH, ProtoUtil.objToString(map));
            ProtoUtil.writeAndFlush(ctx, authProto);
        }
    }

    public Remote getRemote(Remote remote){
        Remote temp = new Remote();
        if(remote != null){
            remote.setHost(StringUtil.isNullOrEmpty(remote.getHost()) ? temp.getHost() : remote.getHost());
            remote.setPort(remote.getPort() == 0 ? temp.getPort() : remote.getPort());
            remote.setUser(StringUtil.isNullOrEmpty(remote.getUser()) ? temp.getUser() : remote.getUser());
            remote.setPassword(StringUtil.isNullOrEmpty(remote.getPassword()) ? temp.getPassword() : remote.getPassword());
            return remote;
        }
        return temp;
    }
}
