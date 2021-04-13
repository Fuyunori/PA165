package cz.muni.fi.pa165.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Servlet preparing data for JSP page.
 *
 * @author PXL
 */
@WebServlet("/podoli")
public class PodoliServlet extends HttpServlet {

    private final static Logger log = LoggerFactory.getLogger(PodoliServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("podoli servlet called, forwarding to podoli.jsp");
        //prepare some data to be displayed
        request.setAttribute("now", new Date());
        request.setAttribute("message", ResourceBundle.getBundle("Texts", request.getLocale()).getString("podoli.message"));

        request.getRequestDispatcher("/WEB-INF/hidden-jsps/podoli.jsp").forward(request, response);
    }

}
