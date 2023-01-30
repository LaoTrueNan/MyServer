package gzq.tomcat;

import gzq.tomcat.util.StringManager;

/**
 * @author guo
 * @date 2022/12/21 11:22
 */

public class Bootstrap {

    private StringManager sm = new StringManager();

    public Bootstrap() {
        String string = sm.getString("welcome.banner");
    }


    public static void main(String[] args) {
        // create connector then start the thread.
        // use command arguments to run the server


        //
    }
}
