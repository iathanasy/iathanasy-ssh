package top.icss.xterm.constants;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * JSch 实体类
 * @author cd.wang
 * @create 2022-09-27 9:50
 */
@Data
public class JSchConnectInfo {

    private WebSocketSession webSocketSession;
    private JSch remoteSsh;
    private Channel channel;
}
