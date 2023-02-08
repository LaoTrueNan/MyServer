package gzq.tomcat;

import javax.servlet.ServletRequest;

/**
 * @author guo
 *
 * corresponding with a {@link ServletRequest}
 * @date 2023/1/28 9:35
 */

public interface Request extends ServletRequest{

    String getUrl();

    String getUri();

}
