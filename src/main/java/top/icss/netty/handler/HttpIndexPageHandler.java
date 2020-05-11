package top.icss.netty.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author cd
 * @desc websocket页面
 * @create 2020/5/9 9:57
 * @since 1.0.0
 */
@Slf4j
public class HttpIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    private final String wsUri;
    private final File INDEX;
    private static final String root = "/";
    private static final String indexUri = "index.html";
    private static final String favicon = "/favicon.ico";
    private static final String ServerName = "netty/1.0";


    public HttpIndexPageHandler(String wsUri) {
        this.wsUri = wsUri;
        String path = ClassLoader.getSystemResource(indexUri).getPath();
        INDEX = new File(path);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        readRequest(request);

        //验证服务请求路径
        String uri = request.uri();
        if (wsUri.equalsIgnoreCase(request.uri())) {
            //往下传递websocket
            ctx.fireChannelRead(request.retain());
        }else{
            //表示消息未发送完毕，示意客户端继续发送剩下的部分
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            if(uri.equals(root) || uri.equals(root + indexUri)) {
                //响应index.html页面
                writeFile(ctx, request, INDEX);
            }else{
                if(uri.equals(favicon)){
                    // Build the response object.
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    ctx.writeAndFlush(response);
                    return;
                }
                uri = uri.replaceAll("/+","/");
                uri = uri.substring(1 ,uri.length());

                //其它文件
                String path = ClassLoader.getSystemResource(uri).getPath();
                File file = new File(path);
                writeFile(ctx, request, file);

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            if(cause instanceof NullPointerException) {
                sendError(ctx, FORBIDDEN);
            }else {
                sendError(ctx, INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     */
    private void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
//        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
//        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
//        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private void setContentTypeHeader(HttpResponse response, File file) {
        String path = file.getName();
        //设置文件格式内容
        if (path.endsWith(".html")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        } else if (path.endsWith(".js")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
        } else if (path.endsWith(".css")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css;charset=UTF-8");
        }
    }

    /**
     * 设置服务名称
     * @param response
     */
    private void setServer(HttpResponse response){
        response.headers().set(HttpHeaderNames.SERVER, ServerName);
    }

    /**
     * 消息未发送完毕，示意客户端继续发送剩下的部分
     * @param ctx
     */
    private void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        setServer(response);
        ctx.writeAndFlush(response);
    }

    /**
     * 返回文件数据
     * @param request
     */
    private void writeFile(ChannelHandlerContext ctx,FullHttpRequest request, File resources) throws Exception {
        RandomAccessFile file = null;
        try {
            if (resources.isHidden() || !resources.exists()) {
                sendError(ctx, NOT_FOUND);
                return;
            }

            //读取文件
            file = new RandomAccessFile(resources, "r");
            long length = file.length();
            //响应客户端
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            setDateAndCacheHeaders(response, resources);
            setContentTypeHeader(response, resources);
            setServer(response);
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if(keepAlive){
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);

            if (ctx.pipeline().get(SslHandler.class) == null) {
                // SSL not enabled - can use zero-copy file transfer.
                // SSL未启用-可以使用零拷贝文件传输。
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, length));
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                // 已启用SSL-无法使用零拷贝文件传输。
                ctx.write(new ChunkedFile(file));
            }

            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            log.error("write file error --> {}",e.getMessage());
        } finally {
            file.close();
        }
    }

    /**
     * 错误返回
     * @param ctx
     * @param status
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        setServer(response);
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    /**
     * 请求消息
     * @param msg
     */
    private void readRequest(FullHttpRequest msg) {
        log.warn("======请求行======");
        log.warn(msg.method() + " " + msg.uri() + " " + msg.protocolVersion());

        log.warn("======请求头======");
        for (String name : msg.headers().names()) {
            log.warn(name + ": " + msg.headers().get(name));

        }

        log.warn("======消息体======");
        log.warn(msg.content().toString(CharsetUtil.UTF_8));

    }
}
