/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.ListOrderOfRoute;
import Model.Route;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Tra Pham
 */
public class RouteDAO {

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    public ArrayList<Route> getAllRoute() {
        ArrayList<Route> list = new ArrayList<>();
        String query = "    \n"
                + "SELECT r.RouteID, r.RouteName, r.StartPoint, STRING_AGG(b.StopName, N' - ') WITHIN GROUP (ORDER BY rs.StopOrder) AS IntermediateStation,\n"
                + "    r.EndPoint, r.StartTime, r.EndTime, r.Frequency\n"
                + "FROM \n"
                + "    [Route] r\n"
                + "LEFT JOIN \n"
                + "    RouteStop rs ON r.RouteID = rs.RouteID\n"
                + "LEFT JOIN \n"
                + "    BusStop b ON rs.StopID = b.StopID\n"
                + "GROUP BY \n"
                + "    r.RouteID, r.RouteName, r.StartPoint, r.EndPoint, r.StartTime, r.EndTime, r.Frequency;";
        try {
            conn = new DBContext().connection;
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Route(rs.getInt("RouteID"), rs.getString("RouteName"), rs.getString("StartPoint"), rs.getString("IntermediateStation"), rs.getString("EndPoint"),
                        rs.getString("StartTime"), rs.getString("EndTime"), rs.getString("Frequency")));
            }
        } catch (Exception e) {

        }

        return list;
    }

    public ArrayList<Route> getRouteByStopID(String stopID1, String stopID2) {
        ArrayList<Route> list = new ArrayList<>();
        String query = "WITH RouteData AS (\n"
                + "    SELECT \n"
                + "        r.RouteID, \n"
                + "        r.RouteName, \n"
                + "        r.StartPoint, \n"
                + "        STRING_AGG(b.StopName, N' - ') WITHIN GROUP (ORDER BY rs.StopOrder) AS IntermediateStation,\n"
                + "        r.EndPoint, \n"
                + "        r.StartTime, \n"
                + "        r.EndTime, \n"
                + "        r.Frequency\n"
                + "    FROM \n"
                + "        [Route] r\n"
                + "    LEFT JOIN \n"
                + "        RouteStop rs ON r.RouteID = rs.RouteID\n"
                + "    LEFT JOIN \n"
                + "        BusStop b ON rs.StopID = b.StopID\n"
                + "    GROUP BY \n"
                + "        r.RouteID, r.RouteName, r.StartPoint, r.EndPoint, r.StartTime, r.EndTime, r.Frequency\n"
                + ")\n"
                + "\n"
                + "SELECT \n"
                + "    a.RouteID, \n"
                + "    a.RouteName, \n"
                + "    a.StartPoint, \n"
                + "    a.IntermediateStation, \n"
                + "    a.EndPoint, \n"
                + "    a.StartTime, \n"
                + "    a.EndTime, \n"
                + "    a.Frequency\n"
                + "FROM \n"
                + "    RouteData a\n"
                + "JOIN \n"
                + "    RouteStop rs1 ON a.RouteID = rs1.RouteID\n"
                + "JOIN \n"
                + "    RouteStop rs2 ON a.RouteID = rs2.RouteID\n"
                + "WHERE \n"
                + "    rs1.StopID = ? \n"
                + "    AND rs2.StopID = ?\n"
                + "ORDER BY \n"
                + "    a.RouteID;";
        try {
            conn = new DBContext().connection;
            ps = conn.prepareStatement(query);
            ps.setString(1, stopID1);
            ps.setString(2, stopID2);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Route(rs.getInt("RouteID"), rs.getString("RouteName"), rs.getString("StartPoint"), rs.getString("IntermediateStation"), rs.getString("EndPoint"),
                        rs.getString("StartTime"), rs.getString("EndTime"), rs.getString("Frequency")));
            }
        } catch (Exception e) {

        }

        return list;
    }

    public void deleteRouteByID(String RouteID) {
        String query = "delete from BusRoute\n"
                + "where RouteID = ?\n"
                + "\n"
                + "delete from RouteStop\n"
                + "where RouteID = ?\n"
                + "\n"
                + "delete from [Route]\n"
                + "where RouteID = ?";
        try {
            conn = new DBContext().connection;
            ps = conn.prepareStatement(query);
            ps.setString(1, RouteID);
            ps.setString(2, RouteID);
            ps.setString(3, RouteID);
            ps.executeUpdate();

        } catch (Exception e) {
        }
    }

    public void createRoute(String RouteName, String StartPoint, String EndPoint, String StartTime,
                        String EndTime, String Frequency, int[] StopID, String[] StopOrder) {
    String insertRouteQuery = "INSERT INTO [Route](RouteName, StartPoint, EndPoint, StartTime, EndTime, Frequency) VALUES (?, ?, ?, ?, ?, ?)";
    String insertRouteStopQuery = "INSERT INTO RouteStop(RouteID, StopID, StopOrder) VALUES (?, ?, ?)";

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = new DBContext().connection;
        conn.setAutoCommit(false); // Start transaction

        // Insert into Route table
        ps = conn.prepareStatement(insertRouteQuery);
        ps.setString(1, RouteName);
        ps.setString(2, StartPoint);
        ps.setString(3, EndPoint);
        ps.setString(4, StartTime);
        ps.setString(5, EndTime);
        ps.setString(6, Frequency);
        ps.executeUpdate();

        // Get the generated RouteID
        rs = ps.getGeneratedKeys();
        int routeID = -1;
        if (rs.next()) {
            routeID = rs.getInt(1);
        }

        // Check if we got a valid RouteID
        if (routeID == -1) {
            throw new Exception("Failed to retrieve RouteID.");
        }

        // Insert into RouteStop table (Batch Processing)
        ps = conn.prepareStatement(insertRouteStopQuery);
        for (int i = 0; i < StopID.length; i++) {
            if (!StopOrder[i].isEmpty()) {
                ps.setInt(1, routeID);
                ps.setInt(2, StopID[i]);
                ps.setInt(3, Integer.parseInt(StopOrder[i]));
                ps.addBatch();
            }
        }
        ps.executeBatch(); // Execute batch insert

        conn.commit(); // Commit transaction

    } catch (Exception e) {
       if (conn != null) {
            try {
                conn.rollback(); // Rollback transaction on error
            }catch (Exception e1) {
           }
        }
        e.printStackTrace();
    }finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (Exception closeEx) {
            closeEx.printStackTrace();
        }
    } 
}


    public Route getRouteByID(String RouteID) {
        String query = " SELECT \n"
                + "        r.RouteID, \n"
                + "        r.RouteName, \n"
                + "        r.StartPoint, \n"
                + "        STRING_AGG(b.StopName, N' - ') WITHIN GROUP (ORDER BY rs.StopOrder) AS IntermediateStation,\n"
                + "        r.EndPoint, \n"
                + "        r.StartTime, \n"
                + "        r.EndTime, \n"
                + "        r.Frequency\n"
                + "    FROM \n"
                + "        [Route] r\n"
                + "    LEFT JOIN \n"
                + "        RouteStop rs ON r.RouteID = rs.RouteID\n"
                + "    LEFT JOIN \n"
                + "        BusStop b ON rs.StopID = b.StopID\n"
                + "	where r.RouteID = ?\n"
                + "    GROUP BY \n"
                + "        r.RouteID, r.RouteName, r.StartPoint, r.EndPoint, r.StartTime, r.EndTime, r.Frequency";
        try {
            conn = new DBContext().connection;
            ps = conn.prepareStatement(query);
            ps.setString(1, RouteID);
            rs = ps.executeQuery();
            while (rs.next()) {
                return (new Route(rs.getInt("RouteID"), rs.getString("RouteName"), rs.getString("StartPoint"), rs.getString("IntermediateStation"), rs.getString("EndPoint"),
                        rs.getString("StartTime"), rs.getString("EndTime"), rs.getString("Frequency")));
            }
        } catch (Exception e) {
        }
        return null;
    }

    public ArrayList<ListOrderOfRoute> getListStopBusHasOrderByRouteID(String RouteID) {
        ArrayList<ListOrderOfRoute> list = new ArrayList<>();
        String query = "select a.StopID, a.StopName, b.StopOrder from \n"
                + "(select rs.StopID,bs.StopName, rs.RouteStopID, rs.StopOrder from \n"
                + "BusStop bs left join RouteStop rs on bs.StopID = rs.StopID\n"
                + "where rs.RouteID = ?) as b  right join\n"
                + "(select * from BusStop) as a \n"
                + "on b.StopID = a.StopID";
        try {
            conn = new DBContext().connection;
            ps = conn.prepareStatement(query);
            ps.setString(1, RouteID);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ListOrderOfRoute(rs.getInt("StopID"), rs.getInt("StopOrder"), rs.getString("StopName")));
            }
        } catch (Exception e) {

        }

        return list;
    }

    public void updateRoute(String RouteID, String RouteName, String StartPoint, String EndPoint, String StartTime,
            String EndTime, String Frequency, int[] StopID, String[] StopOrder) {
        String updateRouteQuery = "update Route\n"
                + "set RouteName = N'" + RouteName + "', StartPoint = N'" + StartPoint + "', EndPoint = N'" + EndPoint + "', \n"
                + "StartTime = N'" + StartTime + "', EndTime = N'" + EndTime + "', Frequency = N'" + Frequency + "'\n"
                + "where RouteID = " + RouteID + "" + "\n";

        String updateRouteStop = "";
        // lay list order cu
        ArrayList<ListOrderOfRoute> oldListOrder = getListStopBusHasOrderByRouteID(RouteID);
        // ket thuc lay list order cu
        for (int i = 0; i < StopID.length; i++) {
            if (!StopOrder[i].isEmpty()) {
                if (oldListOrder.get(i).getStopOrder() == 0) {
                    updateRouteStop += "insert RouteStop(RouteID,StopID,StopOrder)\n"
                            + "values (" + RouteID + "," + StopID[i] + "," + StopOrder[i] + ")";
                } else {
                    updateRouteStop += "update RouteStop\n"
                            + "set \n"
                            + "StopOrder = " + StopOrder[i] + "\n"
                            + "where RouteID = " + RouteID + " and StopID =" + StopID[i] + "\n";
                }

            } else {
                updateRouteStop += "delete RouteStop\n"
                        + "where RouteID = " + RouteID + " and StopID =" + StopID[i] + "\n";
            }
        }
        String finalQuery = updateRouteQuery + updateRouteStop;
        try {
            conn = new DBContext().connection;
            ps = conn.prepareStatement(finalQuery);
//            System.out.println(finalQuery);
            ps.executeUpdate();

        } catch (Exception e) {
        }
    }

}
