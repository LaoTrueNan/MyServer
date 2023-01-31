package gzq.tomcat;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author guo
 * @date 2023/1/31 8:17
 */

public interface Processor {

    void process(Request request,Response response) throws ServletException, IOException;

}
