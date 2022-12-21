package gzq.tomcat.core.loader;

/**
 * @author guo
 * @date 2022/12/21 14:01
 */

/**
 * 类加载器概念
 */
public interface Loader {
    //
    Loader getLoader();
    //
    Class<?> loadClass(String... clazz);
    //

}
