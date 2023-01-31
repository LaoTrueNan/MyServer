package gzq.tomcat.base;

import gzq.tomcat.Response;
import gzq.tomcat.core.logger.Logger;
import gzq.tomcat.util.ConsoleLogger;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author guo
 * @date 2023/1/29 11:18
 */

public class ZQResponse implements Response {
    private final Logger logger = new ConsoleLogger();

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
        } else if(wanted.contains("%20")) {
            // 输出中文时不能使用字节流,一个汉字是两个字节,拆开之后必然导致乱码
            String res = BADREQUEST + separator + HTML + separator + separator + "<!doctype html><html><head><meta charset=\"utf-8\"></head><body><h3>请求路径不能含有空格!</h3></body></html>";
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(oos, StandardCharsets.UTF_8);
            outputStreamWriter.append(res);
            oos.close();
            outputStreamWriter.close();
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
                logger.error(e,"Error occurred while operating files");
            } finally {
                oos.close();
            }
        }
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long length) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}































