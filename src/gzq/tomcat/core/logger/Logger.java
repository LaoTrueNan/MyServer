package gzq.tomcat.core.logger;

/**
 * @author guo
 * @date 2022/12/21 16:29
 */

public interface Logger {

    // which container the logger belongs to
    void log(Throwable e,int level);

    void log(String msg,int level);

    void log(String msg);

    void log(String msg,Throwable e,int level);
}
