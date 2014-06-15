package biz.guanggu;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by Li He on 2014/6/15.
 * @author Li He
 */
@WebFilter
public class LoginValidateFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String URI = request.getRequestURI();

        boolean isRedirect = false;

        if(URI.contains("webservice")&&!URI.contains("webservice/bs_login_info")){
            HttpSession session = request.getSession(true);
            BsLoginInfo bf = (BsLoginInfo) session.getAttribute(Constant.BSLOGININFO);

            if(bf == null){
                isRedirect = true;
                response.sendRedirect(request.getContextPath() + "/convert/temp");
            }
        }

        if(!isRedirect){
            chain.doFilter(request,response);
        }

    }

    public void init(FilterConfig config) throws ServletException {

    }

}
