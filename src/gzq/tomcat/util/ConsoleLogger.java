package gzq.tomcat.util;

import gzq.tomcat.core.logger.Logger;

import java.io.IOException;

/**
 * for different logging level via {@link System#out#println}
 * @author guo
 * @date 2023/1/31 8:28
 */

public class ConsoleLogger implements Logger {

    @Override
    public void info(String info) throws IOException {
        System.out.println(info);
    }

    @Override
    public void debug(String debugInfo) throws IOException {
        System.out.println(debugInfo);
    }

    @Override
    public void warn(String warning) throws IOException {
        System.out.println(warning);
    }

    @Override
    public void error(Throwable e, String msg) throws IOException {
        e.printStackTrace();
        System.err.println(msg);
    }
}
