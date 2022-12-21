package gzq.tomcat.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author guo
 * @date 2022/12/21 12:57
 */

public class StringManager {

    private static final String basePackage = "gzq.tomcat";

    private ResourceBundle bundle;

    public StringManager() {
        bundle = ResourceBundle.getBundle(basePackage + ".LocalStrings");
    }

    public String getString(String key){
        if(bundle==null){
            throw new NullPointerException("ResourceBundle is null");
        }
        if(key==null){
            throw new NullPointerException("Key is null");
        }
        String res;
        try {
            res = bundle.getString(key);
        } catch (MissingResourceException e) {
            res = "nothing found for" + key;
        }
        return res;
    }
}
