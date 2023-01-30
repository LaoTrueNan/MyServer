package gzq.tomcat.base;

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

    public void await(){
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(8087,1,InetAddress.getByName("127.0.0.1"));

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

                ZQResponse zqResponse = new ZQResponse(oos);
                zqResponse.setRequest(zqRequest);

                zqResponse.staticResources();
                if(SHUTDOWN.equalsIgnoreCase(zqRequest.getUrl())){
                    running = false;
                    socket.close();
                }

                iis.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer();
        httpServer.await();
    }
}
