package top.icss.entity;
import lombok.Data;
import lombok.ToString;
import top.icss.ssh.SshServer;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 9:13
 * @since 1.0.0
 */
@ToString
@Data
public class Remote<T> {
    private String host = "127.0.0.1";
    private int port = 22;
    private String user = "root";
    private String password = "root";
    private String identity = "~/.ssh/id_rsa";
    private String passphrase = "";

    /**
     * ssh连接
     */
    private SshServer<T> ssh;
}
