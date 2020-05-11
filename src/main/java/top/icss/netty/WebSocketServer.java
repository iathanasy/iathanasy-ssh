package top.icss.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 9:14
 * @since 1.0.0
 */
@Slf4j
public class WebSocketServer {

    private int port=5891;

    final NioEventLoopGroup boss = new NioEventLoopGroup();
    final EventLoopGroup work = new NioEventLoopGroup();

    /**
     * 启动
     * @throws Exception
     */
    public void start() throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new WebSocketServerInitializer());

        b.bind(port).sync().addListeners((future)->{
            if(future.isSuccess()){
                log.info("websocket server start success ! ");
            }else{
                log.error("websocket server start fail ! ");
            }
        });
    }

    /**
     * 优雅关闭服务
     */
    public void stop(){
        boss.shutdownGracefully();
        work.shutdownGracefully();
    }
}
