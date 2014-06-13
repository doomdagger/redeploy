package biz.guanggu;
/**
 * Created by Li He on 2014/6/13.
 * @author Li He
 */

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener()
public class RunListener implements ServletContextListener {

    // Public constructor is required by servlet spec
    public RunListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("redeploying", Boolean.FALSE);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("redeploying", Boolean.FALSE);
    }


}
