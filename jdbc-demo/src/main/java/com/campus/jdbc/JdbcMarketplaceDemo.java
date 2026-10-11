package com.campus.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * =============================================================================
 * Academic Syllabus Demonstration: Java Database Connectivity (JDBC) Module
 * Project: Peer-to-Peer Campus Marketplace
 * 
 * Demonstrates:
 * 1. JDBC Architecture (Driver, DriverManager, Connection, Statement, PreparedStatement, ResultSet)
 * 2. Proper resource management with try-with-resources
 * 3. Complete CRUD Operations (Create, Read, Update, Delete)
 * 4. Architectural comparison: 2-Tier JDBC vs 3-Tier REST API
 * =============================================================================
 */
public class JdbcMarketplaceDemo {

    // Configurable database credentials
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/campus_marketplace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        printBanner();
        printArchitectureComparison();

        System.out.println("\n[INFO] Initializing JDBC Demonstration...");

        // Step 1: Register and test JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[SUCCESS] MySQL Connector/J Driver successfully loaded into memory.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL Driver not found on classpath: " + e.getMessage());
            return;
        }

        // Execute CRUD lifecycle demonstrations
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("[SUCCESS] Database Connection established: " + conn.getMetaData().getDatabaseProductName() 
                    + " " + conn.getMetaData().getDatabaseProductVersion());

            // 1. SELECT using Statement (Read all categories)
            demonstrateStatementSelect(conn);

            // 2. INSERT using PreparedStatement (Create a temporary listing)
            int generatedListingId = demonstratePreparedStatementInsert(conn);

            // 3. SELECT using PreparedStatement with parameters (Read the inserted item)
            demonstratePreparedStatementSelect(conn, generatedListingId);

            // 4. UPDATE using PreparedStatement (Modify price and condition)
            demonstratePreparedStatementUpdate(conn, generatedListingId);

            // 5. TRANSACTION DEMO (Commit & Rollback)
            demonstrateTransaction(conn);

            // 6. DELETE using PreparedStatement (Delete the created item)
            demonstratePreparedStatementDelete(conn, generatedListingId);

            System.out.println("\n=============================================================================");
            System.out.println(">>> ALL JDBC CRUD OPERATIONS COMPLETED SUCCESSFULLY WITH CLEANUP <<<");
            System.out.println("=============================================================================");

        } catch (SQLException e) {
            System.err.println("[SQL ERROR] Database operation failed: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
    }

    /**
     * Demonstrates Statement and ResultSet for reading categories.
     */
    private static void demonstrateStatementSelect(Connection conn) throws SQLException {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("1. DEMONSTRATION: java.sql.Statement and ResultSet (Read Categories)");
        System.out.println("-----------------------------------------------------------------------------");

        String query = "SELECT id, name, slug FROM categories ORDER BY id ASC LIMIT 5";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.printf("%-5s | %-30s | %-20s%n", "ID", "CATEGORY NAME", "SLUG");
            System.out.println("------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String slug = rs.getString("slug");
                System.out.printf("%-5d | %-30s | %-20s%n", id, name, slug);
            }
        }
    }

    /**
     * Demonstrates PreparedStatement with auto-generated keys for INSERT.
     */
    private static int demonstratePreparedStatementInsert(Connection conn) throws SQLException {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("2. DEMONSTRATION: java.sql.PreparedStatement (INSERT a New Listing)");
        System.out.println("-----------------------------------------------------------------------------");

        String sql = "INSERT INTO listings "
                + "(seller_id, category_id, title, description, price, condition_state, image_url, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Bind parameters (prevents SQL Injection)
            pstmt.setInt(1, 2); // seller_id: Atharva Gokhale
            pstmt.setInt(2, 7); // category_id: Stationery & Drawing Tools
            pstmt.setString(3, "Rotring 0.5mm Mechanical Drafting Pencil");
            pstmt.setString(4, "High precision metallic drafting pencil for engineering drawing assignments.");
            pstmt.setBigDecimal(5, new java.math.BigDecimal("180.00"));
            pstmt.setString(6, "Like New");
            pstmt.setString(7, "assets/items/pencil.png");
            pstmt.setString(8, "available");

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("[INSERT] Rows affected: " + rowsAffected);

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    System.out.println("[SUCCESS] Listing created with Auto-Generated Primary Key ID = " + newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    /**
     * Demonstrates PreparedStatement parameterized SELECT.
     */
    private static void demonstratePreparedStatementSelect(Connection conn, int listingId) throws SQLException {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("3. DEMONSTRATION: PreparedStatement Parameterized Query (SELECT by ID)");
        System.out.println("-----------------------------------------------------------------------------");

        String sql = "SELECT l.id, l.title, l.price, l.condition_state, l.status, u.full_name AS seller_name "
                   + "FROM listings l JOIN users u ON u.id = l.seller_id "
                   + "WHERE l.id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, listingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println(">> Found Listing:");
                    System.out.println("   ID:        " + rs.getInt("id"));
                    System.out.println("   Title:     " + rs.getString("title"));
                    System.out.println("   Price:     ₹" + rs.getBigDecimal("price"));
                    System.out.println("   Condition: " + rs.getString("condition_state"));
                    System.out.println("   Status:    " + rs.getString("status"));
                    System.out.println("   Seller:    " + rs.getString("seller_name"));
                } else {
                    System.out.println("[WARN] Listing not found with ID " + listingId);
                }
            }
        }
    }

    /**
     * Demonstrates PreparedStatement UPDATE.
     */
    private static void demonstratePreparedStatementUpdate(Connection conn, int listingId) throws SQLException {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("4. DEMONSTRATION: PreparedStatement (UPDATE Listing Price & Condition)");
        System.out.println("-----------------------------------------------------------------------------");

        String sql = "UPDATE listings SET price = ?, condition_state = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, new java.math.BigDecimal("150.00")); // Discounted price
            pstmt.setString(2, "Good");
            pstmt.setInt(3, listingId);

            int updated = pstmt.executeUpdate();
            System.out.println("[UPDATE] Rows updated: " + updated);
            System.out.println("[SUCCESS] Price updated to ₹150.00 and condition set to 'Good'.");
        }
    }

    /**
     * Demonstrates Transaction Management (Commit & Rollback).
     */
    private static void demonstrateTransaction(Connection conn) throws SQLException {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("5. DEMONSTRATION: JDBC Transaction Management (Commit & Rollback Demo)");
        System.out.println("-----------------------------------------------------------------------------");

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false); // Begin transaction

        try {
            String sql = "UPDATE users SET year_of_study = year_of_study + 1 WHERE id = 2";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("[TRANSACTION] Incremented year of study for user 2 (Simulated in-memory).");
            }

            // Demonstrate intentional rollback to maintain DB integrity
            conn.rollback();
            System.out.println("[TRANSACTION ROLLBACK] Rollback executed successfully. Database restored.");
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Demonstrates PreparedStatement DELETE.
     */
    private static void demonstratePreparedStatementDelete(Connection conn, int listingId) throws SQLException {
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("6. DEMONSTRATION: PreparedStatement (DELETE Clean-up)");
        System.out.println("-----------------------------------------------------------------------------");

        String sql = "DELETE FROM listings WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, listingId);

            int deleted = pstmt.executeUpdate();
            System.out.println("[DELETE] Rows deleted: " + deleted);
            System.out.println("[SUCCESS] Temporary demonstration listing cleaned up.");
        }
    }

    private static void printBanner() {
        System.out.println("=============================================================================");
        System.out.println("         PEER-TO-PEER CAMPUS MARKETPLACE - JDBC DEMONSTRATION MODULE         ");
        System.out.println("=============================================================================");
    }

    private static void printArchitectureComparison() {
        System.out.println("\n>>> ACADEMIC VIVA NOTE: 2-TIER JDBC vs 3-TIER REST API ARCHITECTURE <<<");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("1. Two-Tier JDBC Architecture (Used in this Demo Module):");
        System.out.println("   [Desktop App / Java Code] ---> [Direct TCP/IP Connection] ---> [MySQL Database]");
        System.out.println("   - Pros: Simple, fast raw execution, direct transactional control.");
        System.out.println("   - Cons: Desktop clients must have DB credentials; insecure over public internet;");
        System.out.println("           bypasses central business logic, difficult to scale and audit.");
        System.out.println();
        System.out.println("2. Three-Tier REST API Architecture (Used in Main Marketplace App):");
        System.out.println("   [Java Swing Desktop GUI] ---> [HTTP/JSON REST API] ---> [PHP 8+ Backend] ---> [MySQL]");
        System.out.println("   - Pros: Client never knows DB credentials; central authentication & authorization;");
        System.out.println("           role-based access control (Student vs Admin), reusable across Web & Mobile.");
        System.out.println("-----------------------------------------------------------------------------");
    }
}
