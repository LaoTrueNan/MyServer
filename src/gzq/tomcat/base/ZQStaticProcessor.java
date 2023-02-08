package gzq.tomcat.base;

import gzq.tomcat.Processor;
import gzq.tomcat.Request;
import gzq.tomcat.Response;
import gzq.tomcat.core.logger.Logger;
import gzq.tomcat.util.ConsoleLogger;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author guo
 * @date 2023/1/31 8:21
 */

public class ZQStaticProcessor implements Processor {

    private final Logger logger = new ConsoleLogger();

    @Override
    public void process(Request request, Response response) throws ServletException, IOException {
        try {
            response.sendStaticResources();
        } catch (ClassCastException e) {

            e.printStackTrace();
        }

    }
}
