package gzq.tomcat.base;

import gzq.tomcat.Processor;
import gzq.tomcat.Request;
import gzq.tomcat.Response;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.Socket;

/**
 * @author guo
 * @date 2022/12/22 8:42
 */

public class StandardProcessor implements Processor {

    void process(Socket socket){
        /*according */
    }

    @Override
    public void process(Request request, Response response) throws ServletException, IOException {

    }
}
