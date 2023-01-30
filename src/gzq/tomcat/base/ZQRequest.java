package gzq.tomcat.base;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guo
 * @date 2023/1/28 14:53
 */

public class ZQRequest {

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

    public String getUrl() {
        return url;
    }

    public String getUri() {
        return uri;
    }
}
