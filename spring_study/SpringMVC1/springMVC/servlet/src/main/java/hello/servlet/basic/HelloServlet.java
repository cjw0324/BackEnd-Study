package hello.servlet.basic;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "hello", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("HelloServlet.service");
        System.out.println(request);
        System.out.println(response);

        String username = request.getParameter("username");
        System.out.println("username = " + username);

        username += " jaewoo";

        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(username);

    }

}
