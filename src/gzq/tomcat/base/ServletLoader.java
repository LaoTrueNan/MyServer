package gzq.tomcat.base;

import gzq.tomcat.core.loader.Loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.logging.Logger;

/**
 * @author guo
 * @date 2022/12/21 16:10
 */

public class ServletLoader implements Loader {
    // TODO 添加一个日志组件
    private static final String WEB_ROOT = System.getProperty("user.dir")+ File.separator+"web_root";
    private ClassLoader classLoader = null;
    public ServletLoader() {
        try {
            URL[] urls = new URL[1];
            File classPath = new File(WEB_ROOT);
            urls[0] = new URL(null, new URL("file", null,classPath.getCanonicalPath() + File.separator).toString(), (URLStreamHandler) null);
            classLoader = new URLClassLoader(urls);
        } catch (IOException e) {

        }
    }

    @Override
    public Loader getLoader() {
        return new ServletLoader();
    }

    @Override
    public Class<?> loadClass(String... clazz) {
        return null;
    }
}
