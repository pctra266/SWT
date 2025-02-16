package DAO;

import DAO.AppContextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import jakarta.servlet.ServletContext;
public class DBContext {
    private static String user;
    private static String pass;
    protected Connection connection;
    
    static {
        ServletContext context = AppContextListener.getServletContext();
        if (context != null) {
            user = context.getInitParameter("DB_USER");
            pass = context.getInitParameter("DB_PASS");
        }
    }   
    public DBContext()
    {

        try {
            String url = "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=PRJ301_Group3_BusSystem;encrypt=true;trustServerCertificate=true";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) {
        System.out.println(user);
        System.out.println(pass);
    }
}
