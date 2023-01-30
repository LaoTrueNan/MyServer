package gzq.tomcat.base;

import gzq.tomcat.core.connector.Connector;
import gzq.tomcat.util.StringManager;

/**
 * @author guo
 * @date 2022/12/21 11:50
 */

public class StandardConnector implements Connector {

    private static final String info = "Tomcat replica, version 0.0.1";

    private StringManager sm = new StringManager();
    public StandardConnector() {
        sm.getString("connector.loaded");
    }

    public static void main(String[] args) {
        String a = "Host: localhost:8087";
        String[] split = a.split(":\\s");
        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]+"●●●●"+i);
        }
    }

}
