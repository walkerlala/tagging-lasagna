package com.lasagna.service;

import javax.servlet.*;
import java.io.IOException;
import javax.servlet.http.*;

public class MyDispatch implements Filter {

    public void init(FilterConfig config) {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        //just forward it
        chain.doFilter(request, response);
        /*
        if(path.startsWith("/dynamic")){
            chain.doFilter(request, response);
        }else{
            //else we dispatch to /
            request.getRequestDispatcher("/pages" + path).forward(request, response);
        }
        */
    }

    public void destroy() {
    }
}
