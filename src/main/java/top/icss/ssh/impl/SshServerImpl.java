package top.icss.ssh.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.icss.entity.Remote;
import top.icss.ssh.SshServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cd
 * @desc
 * @create 2020/5/11 15:36
 * @since 1.0.0
 */
@Slf4j
@Data
public class SshServerImpl implements SshServer<Connection> {

    private Connection channel;
    private Session session;
    private Remote<Connection> remote;
    private static String  DEFAULTCHART="UTF-8";

    @Override
    public void connect(Remote<Connection> remote) {

        try {
            channel = new Connection(remote.getHost(), remote.getPort());
            channel.connect(); //连接
            //auth
            channel.authenticateWithPassword(remote.getUser(), remote.getPassword());

            this.remote = remote;
            remote.setSsh(this);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("登录主机 [{}] 失败!", remote.getHost());
        }
    }

    @Override
    public List<String> exec(String command) throws IOException{
        List<String> list = null;

        if(channel == null){
            //执行连接
            connect(getRemote());
        }
        //打开一个会话
        session= channel.openSession();
        //执行命令
        session.execCommand(command);

        list = processStdout(session.getStdout(), DEFAULTCHART);
        //如果为得到标准输出为空，说明脚本执行出错了
        if(list.size() <= 0){
            list = processStdout(session.getStderr(),DEFAULTCHART);
        }

        return list;
    }


    /**
     * 关闭连接
     */
    @Override
    public void close() {
        try {
            if (channel != null) {
                channel.close();
            }
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            log.error("关闭异常{} ", e.getMessage());
        }
    }

    /**
     * 解析脚本执行返回的结果集
     * @param in 输入流对象
     * @param charset 编码
     * @return
     *       以纯文本的格式返回
     */
    private List<String> processStdout(InputStream in, String charset){
        InputStream  stdout = new StreamGobbler(in);
        List<String> list = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout,charset));
            String line=null;
            while((line=br.readLine()) != null){
                list.add(line);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("解析脚本出错："+e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("解析脚本出错："+e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
