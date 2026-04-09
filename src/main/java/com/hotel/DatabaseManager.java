package com.hotel;

import com.hotel.Main.Customer;
import com.hotel.Main.FoodOrder;
import com.hotel.Main.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:hotel.db";

    public static synchronized void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            

            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "roomNumber TEXT PRIMARY KEY," +
                    "type TEXT," +
                    "price REAL," +
                    "status TEXT," +
                    "customerName TEXT" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "name TEXT PRIMARY KEY," +
                    "contact TEXT" +
                    ")");


            stmt.execute("CREATE TABLE IF NOT EXISTS food_orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "customerName TEXT," +
                    "itemName TEXT," +
                    "quantity INTEGER," +
                    "totalCost REAL" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static synchronized List<Room> fetchAllRooms() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
            while (rs.next()) {
                Room r = new Room(rs.getString("roomNumber"), rs.getString("type"), rs.getDouble("price"));
                r.statusProperty().set(rs.getString("status"));
                r.customerNameProperty().set(rs.getString("customerName"));
                rooms.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }

    public static synchronized void insertRoom(Room r) {
        String sql = "INSERT INTO rooms(roomNumber, type, price, status, customerName) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, r.getRoomNumber());
            pstmt.setString(2, r.getType());
            pstmt.setDouble(3, r.getPrice());
            pstmt.setString(4, r.getStatus());
            pstmt.setString(5, r.getCustomerName());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static synchronized void removeRoom(String roomNumber) {
        String sql = "DELETE FROM rooms WHERE roomNumber = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static synchronized void updateRoom(Room r) {
        String sql = "UPDATE rooms SET status = ?, customerName = ? WHERE roomNumber = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, r.getStatus());
            pstmt.setString(2, r.getCustomerName());
            pstmt.setString(3, r.getRoomNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    public static synchronized List<Customer> fetchAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) {
                customers.add(new Customer(rs.getString("name"), rs.getString("contact")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return customers;
    }

    public static synchronized void insertCustomer(Customer c) {
        String sql = "INSERT INTO customers(name, contact) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getContact());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static synchronized void removeCustomer(String name) {
        String sql = "DELETE FROM customers WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    public static synchronized List<FoodOrder> fetchAllFoodOrders() {
        List<FoodOrder> orders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM food_orders")) {
            while (rs.next()) {
                orders.add(new FoodOrder(rs.getString("customerName"), rs.getString("itemName"), rs.getInt("quantity"), rs.getDouble("totalCost")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public static synchronized void insertFoodOrder(FoodOrder fo) {
        String sql = "INSERT INTO food_orders(customerName, itemName, quantity, totalCost) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fo.getCustomerName());
            pstmt.setString(2, fo.getItemName());
            pstmt.setInt(3, fo.getQuantity());
            pstmt.setDouble(4, fo.getTotalCost());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static synchronized void removeFoodOrder(FoodOrder fo) {

        String sql = "DELETE FROM food_orders WHERE customerName = ? AND itemName = ? AND quantity = ? AND totalCost = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fo.getCustomerName());
            pstmt.setString(2, fo.getItemName());
            pstmt.setInt(3, fo.getQuantity());
            pstmt.setDouble(4, fo.getTotalCost());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static synchronized void removeFoodOrdersForCustomer(String customerName) {
        String sql = "DELETE FROM food_orders WHERE customerName = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerName);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
