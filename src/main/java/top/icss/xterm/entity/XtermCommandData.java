package top.icss.xterm.entity;

import lombok.Data;
import lombok.ToString;

/**
 * @author cd.wang
 * @create 2022-09-27 9:48
 */
@ToString
@Data
public class XtermCommandData {

    //操作
    private String operate;
    private String host;
    //端口号默认为22
    private Integer port = 22;
    private String username;
    private String password;
    private String command = "";
}
