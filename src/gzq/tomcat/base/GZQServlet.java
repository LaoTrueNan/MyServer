/*
 * author guo.zhiqiang
 * welcome to my own tomcat replica
 */
package gzq.tomcat.base;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * My own custom {@link Servlet} implement, just simply overrides 5 methods in {@link Servlet}
 * in this class, {@link #init(ServletConfig)} and {@link #destroy()} will be invoked only once
 * but {@link #service(ServletRequest, ServletResponse)} will be called many times during the sevlet's lifetime
 * need to be loader by classloader so as to be accessible by {@link ZQResponse}
 * @date 2023/1/30 11:22
 */
public class GZQServlet implements Servlet {
    /**
     * initialize the connection or some other resources
     * @param config the information that represents the servlet
     * @throws ServletException thrown when something wrong in configuration
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化方法
        System.out.println("GZQServlet初始化.");
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    /**
     * codes that handle request
     * @param req the body and url that make up the request
     * @param res the response
     * @throws ServletException thrown when cannot handle the request
     * @throws IOException thrown when there is problem during connection and input/output
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        PrintWriter writer = res.getWriter();
        writer.println("Hello from GZQServlet.");
        writer.print("And bye~");
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    /**
     * release the resources that held by this servlet
     */
    @Override
    public void destroy() {
        // 销毁方法
        System.out.println("GZQServlet销毁.");
    }
}
