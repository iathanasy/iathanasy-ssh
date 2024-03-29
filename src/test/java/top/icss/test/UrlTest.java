package top.icss.test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * @author cd
 * @desc
 * @create 2020/5/9 10:09
 * @since 1.0.0
 */
public class UrlTest {

    private static final Pattern INSECURE_URI = Pattern.compile(".*[css\\/js\\/&\"].*");

    public static void main(String[] args) throws URISyntaxException {
        //项目根路径

        String path = ClassLoader.getSystemResource("static/index1111.html").getPath();
        System.out.println(path);

        URL resource = ClassLoader.getSystemClassLoader().getResource("static/index1111.html");
        System.out.println(resource.toURI());
        String uri = "////css//xterm.css";
        uri = uri.replaceAll("/+","/");
        System.out.println(uri.substring(1 ,uri.length()));
        //System.out.println(uri.substring());

        System.out.println(1 << 8 | 220);
        System.out.println(2 << 8 | 220);
        System.out.println(3 << 8 | 220);
        System.out.println(4 << 8 | 220);
        System.out.println(5 << 8 | 220);
        System.out.println(6 << 8 | 220);

       int threadNum = Runtime.getRuntime().availableProcessors();
        System.out.println(threadNum);
    }
}
