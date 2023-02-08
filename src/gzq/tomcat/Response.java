package gzq.tomcat;

import javax.servlet.ServletResponse;

/**
 * @author guo
 * @date 2023/1/28 9:35
 */

public interface Response extends ServletResponse {
    /**
     * TODO some getters used for getting info of a response.
     */
    void sendStaticResources();

    void responseServlet();
}
