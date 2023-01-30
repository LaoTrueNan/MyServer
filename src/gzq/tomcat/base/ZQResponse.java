package gzq.tomcat.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author guo
 * @date 2023/1/29 11:18
 */

public class ZQResponse {

    /*STATUS CODES*/
    /**
     * 响应成功 {@code 200}
     */
    private final static String SUCCESS = "HTTP/1.1 200 OK";

    /**
     * 404
     */
    private final static String NOTFOUND = "HTTP/1.1 404 Not Found";

    /**
     * 502
     */
    private final static String BADRGATEWAY = "HTTP/1.1 502 Bad GateWay";

    /**
     * 400
     */
    private static final String BADREQUEST = "HTTP/1.1 400 Bad Request";

    /**
     * 405
     */
    private final static String METHODNOTALLOWED = "HTTP/1.1 405 Method Not Allowed";

    /**
     * 500
     */
    private static final String SERVERERROR = "HTTP/1.1 500 Internal Server Error";

    /*MIME TYPES*/
    /**
     * HTML
     */
    private static final String HTML = "Content-Type: text/html;charset=utf-8";

    /**
     * TEXT
     */
    private static final String TEXT = "Content-Type: text/plain;charset=utf-8";

    /**
     * 分隔符
     */
    private static final String separator = "\r\n";

    private static final String WEBROOT = System.getProperty("user.dir") + File.separator + "web_root";

    /**
     * 对应的输出流
     */
    private OutputStream oos;

    /**
     * 对应的请求
     */
    private ZQRequest request;

    public ZQResponse(OutputStream oos) {
        this.oos = oos;
    }

    public ZQResponse(ZQRequest request) {
        this.request = request;
    }

    public void setRequest(ZQRequest zqRequest) {
        request = zqRequest;
    }

    public void staticResources() throws IOException {
        // 先查询请求的资源名称
        String wanted = request.getUrl();
        if (wanted.equalsIgnoreCase(HttpServer.SHUTDOWN)) {
            String res = SUCCESS + separator + HTML + separator +separator + "<p>closed</p>";
            oos.write(res.getBytes(StandardCharsets.UTF_8));
            oos.close();
        } else {


            File wantedFile = new File(WEBROOT + File.separator + wanted);

            try {
                if (!wantedFile.exists()) {
                    String res = NOTFOUND + separator + HTML + separator + separator + "<h2>Nothing found for " + wanted + "!!</h2>";
                    oos.write(res.getBytes(StandardCharsets.UTF_8));
                } else {
                    FileInputStream fis = new FileInputStream(wantedFile);
                    String res = SUCCESS + separator + TEXT + separator+ separator ;
                    oos.write(res.getBytes(StandardCharsets.UTF_8));
                    byte[] buf = new byte[1024];
                    int len = fis.read(buf, 0, 1014);
                    while (len != -1) {
                        oos.write(buf, 0, len);
                        len = fis.read(buf, 0, 1024);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                // TODO 加全局日志
            } finally {
                oos.close();
            }
        }
    }


}































