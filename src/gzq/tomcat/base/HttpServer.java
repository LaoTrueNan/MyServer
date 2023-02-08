package gzq.tomcat.base;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author guo
 * @date 2023/1/28 14:49
 */
public class HttpServer {

    static final String SHUTDOWN = "shutdown";

    private boolean running = true;

    private int port = 8087;

    public void setPort(int port) {
        this.port = port;
    }

    public void await() throws IOException {
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(port,1,InetAddress.getByName("127.0.0.1"));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while(running){
            Socket client = null;
            InputStream iis = null;
            OutputStream oos = null;
            try {
                client = socket.accept();
                iis = client.getInputStream();
                oos = client.getOutputStream();

                ZQRequest zqRequest = new ZQRequest(iis);
                zqRequest.parse();
                if(zqRequest.getUrl()==null){
                    continue;
                }

                ZQResponse zqResponse = new ZQResponse(oos);
                zqResponse.setRequest(zqRequest);

                StandardProcessor standardProcessor = new StandardProcessor();
                standardProcessor.process(zqRequest,zqResponse);

                if(SHUTDOWN.equalsIgnoreCase(zqRequest.getUrl())){
                    running = false;
                    socket.close();
                }
            } catch (ServletException e) {
                e.printStackTrace();
            } finally {
                iis.close();
                client.close();
            }

        }
    }



    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer();
        if(args[0]!=null){
            try {
                int i = Integer.parseInt(args[0]);
                httpServer.setPort(i);
            } catch (NumberFormatException ignored) {

            }
        }
        httpServer.await();
    }
}
