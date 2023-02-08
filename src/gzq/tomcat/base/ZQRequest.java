package gzq.tomcat.base;

import gzq.tomcat.Request;

import javax.servlet.*;
import java.io.*;
import java.util.*;

/**
 * @author guo
 * @date 2023/1/28 14:53
 */

public class ZQRequest implements Request {

    private InputStream iis;

    /**
     * 请求头
     */
    private HashMap<String,String> headers = new HashMap<>();

    /**
     * 路径
     */
    private String url;

    /**
     * 请求全路径
     */
    private String uri;

    /**
     * 请求的全部内容
     */
    private String body;

    /**
     * 构造函数,传入套接字的输入流 来生成对应的{@link ZQRequest}实体
     * @param is
     */
    public ZQRequest(InputStream is) {
        iis = is;
    }

    /**
     * 解析{@link #iis}分别解析成{@link #headers},{@link #url}等详细信息
     */
    public void parse(){
        try {
            BufferedInputStream bis = new BufferedInputStream(iis);

            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[2048];
            iis.read(buf,0,2048);
            sb.append(new String(buf,0,2048));

            body=sb.toString();
            // 路径第一次出现的位置
            int firstPos = body.indexOf("/");
            if(firstPos!=-1){
                String _url = body.substring(firstPos+1,body.indexOf(" ",firstPos));
                url = _url;
            }

            System.out.println(url);
            pushHeaders();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushHeaders(){
        String[] split = body.split("\n\r");
        for (int i = 1; i < split.length; i++) {
            String whole = split[i];
        }
    }
    public String getHeader(String headerName) {
        String s = headers.get(headerName);
        if(s==null){
            s = "";
        }
        return s;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {

        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
