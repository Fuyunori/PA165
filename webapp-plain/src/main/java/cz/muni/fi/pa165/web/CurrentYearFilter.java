package cz.muni.fi.pa165.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Filter which adds an attribute with the current year to every request.
 *
 * @author PXL
 */
@WebFilter("/*")
public class CurrentYearFilter implements Filter {

    private final static Logger log = LoggerFactory.getLogger(CurrentYearFilter.class);

    public void doFilter(ServletRequest r, ServletResponse s, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) r;
        HttpServletResponse response = (HttpServletResponse) s;

        SimpleDateFormat currentYear = new SimpleDateFormat("yyyy", request.getLocale());
        String currentYearString = currentYear.format(new Date());
        request.setAttribute("currentYear", currentYearString);

        filterChain.doFilter(request, response);
        log.trace(request.getRequestURL().toString());
    }

    public void init(FilterConfig filterConfig) {
        log.debug("filter initialized ...");
    }

    public void destroy() {
    }
}
