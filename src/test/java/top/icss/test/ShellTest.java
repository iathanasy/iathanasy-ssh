package top.icss.test;

import com.jcraft.jsch.*;

import javax.swing.*;
import java.io.FilterInputStream;
import java.io.IOException;

/**
 * @author cd.wang
 * @create 2022-09-27 13:42
 */
public class ShellTest {
    public static void main(String[] args) throws JSchException {
        JSch jsch = new JSch();
        //jsch.setKnownHosts("C:\\Users\\XXX\\.ssh\\known_hosts");
        String host = JOptionPane.showInputDialog("Enter hostname", "192.168.0.211");
        int port = 22;
        String username = JOptionPane.showInputDialog("Enter username", "root");
        Session session = jsch.getSession(username, host, port);
        String passwd = JOptionPane.showInputDialog("Enter password", "123456");
        session.setPassword(passwd);
        session.setUserInfo(new MyUserInfo());
        session.connect(30000);
        Channel channel = session.openChannel("shell");
        //((ChannelShell)channel).setAgentForwarding(true);
        //使用Window的问题
        channel.setInputStream(new FilterInputStream(System.in) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return in.read(b, off, (len > 1024 ? 1024 : len));
            }
        });
//        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        //去除控制台彩色输出
//        ((ChannelShell) channel).setPtyType("vt102");
        ((ChannelShell) channel).setEnv("LANG", "zh_CN");
        channel.connect(3 * 1000);
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptYesNo(String message) {
            Object[] options = {"yes", "no"};
            int foo = JOptionPane.showOptionDialog(null,
                    message,
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return foo == 0;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo) {
            return null;
        }
    }
}
