package gzq.tomcat.core.logger;

import java.io.IOException;

/**
 * @author guo
 * @date 2023/1/31 8:27
 */

public interface Logger {

    void info(String info) throws IOException;

    void debug(String debugInfo) throws IOException;

    void warn(String warning) throws IOException;

    void error(Throwable e, String msg) throws IOException;

}
