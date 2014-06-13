package biz.guanggu;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Li He on 2014/6/13.
 * @author LI HE
 */
@WebServlet("/run")
public class RunServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        boolean canRun = true;

        synchronized (getServletContext().getAttribute("redeploying")){
            Boolean status = (Boolean)getServletContext().getAttribute("redeploying");

            if(status)
                canRun = false;
            else
                getServletContext().setAttribute("redeploying", Boolean.TRUE);
        }

        if (canRun){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RedeployUtil.deploy(getServletContext()
                            , "J:\\web\\dcerp\\com\\iit\\dcerp\\0.1.0\\dcerp-0.1.0.war"
                    , "J:\\web\\dcerp\\dcerp.war"
                    , "J:\\web\\dcerp\\in.war", "J:\\web\\dcerp\\out.war");
                }
            }).start();
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");

        PrintWriter writer = response.getWriter();

        writer.println("<h1>Redeploy Worker</h1>");
        writer.println("<h3>Status: "+(canRun?"Idle, So Redeploy starts now!":"Busy, So Wait for next loop!")+"</h3>");

        writer.flush();
        writer.close();
    }
}
