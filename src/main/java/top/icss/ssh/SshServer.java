package top.icss.ssh;

import top.icss.entity.Remote;

import java.util.List;

/**
 * @author cd
 * @desc ssh
 * @create 2020/5/11 15:25
 * @since 1.0.0
 */
public interface SshServer<T> {

    /**
     * 连接
     * @param remote
     */
    void connect(Remote<T> remote);

    /**
     * 执行指令
     * @param command
     * @return
     */
    List<String> exec(String command) throws Exception;

    /**
     * 关闭连接
     */
    void close();
}
