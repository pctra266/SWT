/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import DAO.BusDAO;
import DAO.BusStopDAO;
import DAO.RoleDAO;
import DAO.RouteDAO;
import DAO.StopDAO;
import DAO.UserDAO;
import Model.Bus;
import Model.BusStop;
import Model.ListOrderOfRoute;
import Model.Role;
import Model.Route;
import Model.Stop;
import Model.User;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;

/**
 *
 * @author Tra Pham
 */
@WebServlet(name = "LoadController", urlPatterns = {"/load"})
public class LoadController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoadController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LoadController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    HttpSession session = request.getSession();
    User currentUser = (User) session.getAttribute("currentUser");

    if (action == null) {
        loadHomePage(request, response);
        return;
    }

    if (currentUser == null) {
        response.sendRedirect("AccessDenied.jsp");
        return;
    }

    switch (action) {
        case "loadUpdateRoute" -> loadUpdateRoute(request, response);
        case "loadCreateRoute" -> loadCreateRoute(request, response);
        case "loadUpdateBus" -> loadUpdateBus(request, response);
        case "loadUpdateUser", "loadCreateUser" -> handleUserManagement(request, response, action, currentUser);
        case "logout" -> {
            session.invalidate();
            response.sendRedirect("load");
            }
        default -> response.sendRedirect("load");
    }
}

/**
 * Tải trang chủ với danh sách tuyến đường và điểm dừng xe buýt.
 */
private void loadHomePage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RouteDAO daoRoute = new RouteDAO();
    BusStopDAO daoBus = new BusStopDAO();

    request.setAttribute("listR", daoRoute.getAllRoute());
    request.setAttribute("listBS", daoBus.getAllBusStop());
    request.getRequestDispatcher("HomePage.jsp").forward(request, response);
}

/**
 * Tải trang cập nhật tuyến đường.
 */
private void loadUpdateRoute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RouteDAO daoRoute = new RouteDAO();
    StopDAO daoStop = new StopDAO();

    String routeId = request.getParameter("RouteID");
    request.setAttribute("updateRoute", daoRoute.getRouteByID(routeId));
    request.setAttribute("listStopHasOrder", daoRoute.getListStopBusHasOrderByRouteID(routeId));
    request.setAttribute("listStop", daoStop.getAllStop());

    request.getRequestDispatcher("UpdateRoute.jsp").forward(request, response);
}

/**
 * Tải trang tạo tuyến đường.
 */
private void loadCreateRoute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    StopDAO daoStop = new StopDAO();
    request.setAttribute("listStop", daoStop.getAllStop());
    request.getRequestDispatcher("CreateRoute.jsp").forward(request, response);
}

/**
 * Tải trang cập nhật thông tin xe buýt.
 */
private void loadUpdateBus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RouteDAO daoRoute = new RouteDAO();
    BusDAO busDao = new BusDAO();

    request.setAttribute("listRoute", daoRoute.getAllRoute());
    request.setAttribute("updateBus", busDao.getBusByID(request.getParameter("BusID")));
    request.getRequestDispatcher("UpdateBus.jsp").forward(request, response);
}

/**
 * Xử lý tải trang cập nhật hoặc tạo người dùng.
 */
private void handleUserManagement(HttpServletRequest request, HttpServletResponse response, String action, User currentUser) throws ServletException, IOException {
    if (currentUser.getRoleID() != 1) {
        response.sendRedirect("AccessDenied.jsp");
        return;
    }

    UserDAO userDao = new UserDAO();
    RoleDAO daoRole = new RoleDAO();

    String error = request.getParameter("error");
    if ("1".equals(error)) {
        request.setAttribute("mess", "User name already exists, try another name");
    }

    if (action.equals("loadUpdateUser")) {
        request.setAttribute("userUpdate", userDao.getUserByID(request.getParameter("UserID")));
        request.setAttribute("listRole", daoRole.getAllRole());
        request.getRequestDispatcher("UpdateUser.jsp").forward(request, response);
    } else {
        request.setAttribute("listRole", daoRole.getAllRole());
        request.getRequestDispatcher("CreateUser.jsp").forward(request, response);
    }
}

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String stopId1 = request.getParameter("StopID1");
        String stopId2 = request.getParameter("StopID2");

        // get all Route by StopID
        RouteDAO daoRoute = new RouteDAO();
        ArrayList<Route> listRoute = daoRoute.getRouteByStopID(stopId1, stopId2);
        request.setAttribute("listR", listRoute);

        // get all BusStop
        BusStopDAO daoBus = new BusStopDAO();
        ArrayList<BusStop> listBusStop = daoBus.getAllBusStop();
        request.setAttribute("listBS", listBusStop);

        request.setAttribute("StopID1", stopId1);
        request.setAttribute("StopID2", stopId2);

        request.getRequestDispatcher("HomePage.jsp").forward(request, response);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
