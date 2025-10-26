package api.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/user")
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        UserModel model = new UserModel();
        User user = model.getUser();
        String json = String.format("{\"id\": \"%s\", \"name\": \"%s\"}", user.getId(), user.getName());
        response.getWriter().write(json);
    }
}


