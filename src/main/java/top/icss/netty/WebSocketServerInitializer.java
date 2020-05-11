package top.icss.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import top.icss.netty.handler.HttpIndexPageHandler;
import top.icss.netty.handler.WebSocketServerAuthHandler;
import top.icss.netty.handler.WebSocketServerHandler;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 9:22
 * @since 1.0.0
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private final String websocketPath = "/websocket";

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline p = channel.pipeline();
        p.addLast(new HttpServerCodec());
        //分段聚合
        p.addLast(new HttpObjectAggregator(1024*64));
        //分块
        p.addLast(new ChunkedWriteHandler());

        //跳转index
        p.addLast(new HttpIndexPageHandler(websocketPath));
        //websocket
        p.addLast(new WebSocketServerProtocolHandler(websocketPath));
        //授权
        p.addLast(new WebSocketServerAuthHandler());
        //业务
        p.addLast(new WebSocketServerHandler());

    }
}
