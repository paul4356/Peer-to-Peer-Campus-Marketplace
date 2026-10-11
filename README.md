# Peer-to-Peer Campus Marketplace

A full-stack, campus-exclusive desktop and web marketplace designed for university students to buy, sell, and exchange used academic resources (engineering textbooks, scientific calculators, lab coats, bicycles, lab equipment, and graphics drafting tools) safely within their collegiate community.

---

## 📸 Interface Preview & Design Inspiration

The desktop application and web module are built with a modern dark theme with vivid orange accents (`#F26440` / `#FF6B35`), responsive product card grids, condition pills (`GOOD`, `NEW`, `LIKE NEW`, `ACCEPTABLE`), Indian Rupee pricing (`₹`), seller credentials, and student verification indicators.

---

## 🏛️ System Architecture

The platform adheres to a clean **3-Tier Client-Server Architecture**:

```
+-------------------------------------------------------+
|                 1. PRESENTATION TIER                  |
|  Java Swing GUI (FlatLaf Dark Theme, Desktop Client)  |
|         or Web Learning Client-Side Portal            |
+---------------------------+---------------------------+
                            |
                   HTTP/JSON REST API
                 (Bearer Authentication)
                            |
+---------------------------v---------------------------+
|                  2. APPLICATION TIER                  |
|                 PHP 8.3+ REST API                     |
| (Front Controller, Auth Middleware, RBAC Controller)  |
+---------------------------+---------------------------+
                            |
                     PDO Prepared Stmts
                       (TCP Port 3306)
                            |
+---------------------------v---------------------------+
|                    3. DATA TIER                       |
|        MySQL / MariaDB or PostgreSQL                    |
| (users, categories, listings, wishlist, reports, logs)|
+-------------------------------------------------------+
```

> **Viva Note on Architecture:**
> - **Main Application:** Java Swing communicates **strictly through HTTP REST APIs** with the PHP backend. PHP communicates with MySQL using PDO with prepared statements. Java Swing *never* connects directly to MySQL.
> - **JDBC Demonstration Module:** A separate standalone module demonstrates raw 2-tier database connectivity (`DriverManager`, `Statement`, `PreparedStatement`, `ResultSet`, `Transaction Management`) for academic syllabus criteria.

---

## 🗄️ Database Design (Mermaid ER Diagram)

```mermaid
erDiagram
    users ||--o{ listings : "creates/sells"
    users ||--o{ wishlist : "saves"
    users ||--o{ reports : "submits"
    users ||--o{ admin_audit_logs : "audited"
    categories ||--o{ listings : "categorizes"
    listings ||--o{ wishlist : "bookmarked_in"
    listings ||--o{ reports : "reported_in"

    users {
        int id PK
        varchar student_id UK
        varchar full_name
        varchar email UK
        varchar password_hash
        varchar phone
        varchar department
        int year_of_study
        enum role "student, admin"
        enum verification_status "pending, approved, rejected"
        varchar api_token UK
        timestamp created_at
    }

    categories {
        int id PK
        varchar name UK
        varchar slug UK
        varchar description
        varchar icon_name
        timestamp created_at
    }

    listings {
        int id PK
        int seller_id FK
        int category_id FK
        varchar title
        text description
        decimal price
        enum condition_state "New, Like New, Good, Acceptable"
        varchar image_url
        enum status "available, sold, reserved, inactive"
        int views_count
        timestamp created_at
    }

    wishlist {
        int id PK
        int user_id FK
        int listing_id FK
        timestamp created_at
    }

    reports {
        int id PK
        int reporter_id FK
        int listing_id FK
        varchar reason
        text details
        enum status "pending, reviewed, dismissed"
        timestamp created_at
    }

    admin_audit_logs {
        int id PK
        int admin_id FK
        varchar action
        varchar target_type
        int target_id
        text details
        varchar ip_address
        timestamp created_at
    }
```

### Purpose of Database Tables

| Table | Purpose |
|---|---|
| `users` | Stores student and admin credentials, bcrypt password hashes, academic department, and administrator verification statuses (`pending`, `approved`, `rejected`). |
| `categories` | Organizes campus essentials (Books, Calculators, Electronics, Lab Equipment, Bicycles, Lab Coats, Drawing Tools). |
| `listings` | Item titles, descriptions, prices, condition ratings, view counters, availability status, and seller foreign keys. |
| `wishlist` | Bookmarks saved by students for items they plan to purchase, with unique compound constraint `(user_id, listing_id)`. |
| `reports` | Inappropriate item or prohibited listing flags filed by students for administrative review. |
| `admin_audit_logs` | Immutable security log tracking verification approvals, rejections, and moderation deletions with admin ID and IP. |

---

## 🛠️ Technology Stack

- **Desktop GUI:** Java 17+ / Java 26 (Swing, FlatLaf Dark Theme, Non-blocking `SwingWorker`)
- **Backend API:** PHP 8+ (PSR-4 routing, PDO, Prepared Statements, Bcrypt)
- **Database:** MySQL / MariaDB or PostgreSQL (Foreign Keys, Indexes)
- **Build Tool:** Apache Maven 3.9+
- **Syllabus Module 1 (JDBC):** MySQL Connector/J 8.3+, try-with-resources CRUD
- **Syllabus Module 2 (Web):** Semantic HTML5, CSS Grid, Flexbox, Vanilla JavaScript ES6+

---

## 🚀 Quick Start Guide (Windows)

### Prerequisites
1. **XAMPP** installed at `C:\xampp` (providing Apache, MySQL/MariaDB, and PHP).
2. **Java JDK 17+** (Installed at `C:\Program Files\Java\jdk-26.0.1` or on system PATH).

### 1. Launch Entire Application with 1-Click
Double click **`run-app.bat`** in the project root:
- Checks and starts MySQL daemon (port 3306).
- Checks and starts PHP REST API server on `http://127.0.0.1:8000`.
- Launches the Java Swing Desktop Client.

## ☁️ Deploy on Render

This deploys the browser marketplace and PHP API together. The Java Swing
desktop client and JDBC demo run locally and are not part of the web service.

1. In Render, create a **PostgreSQL** database. Choose the region you plan to
   use for the web service and wait for the database status to become
   **Available**.
2. Create a **Web Service** from this GitHub repository. Choose **Docker**,
   leave **Root Directory** blank, and select the same region as the database.
   Render will build the repository-root `Dockerfile`.
3. In the web service's **Environment** settings, add this variable:

   | Key | Value |
   |---|---|
   | `DATABASE_URL` | The PostgreSQL database's **Internal Database URL** copied from its Render **Connect** section |

   Keep the URL private. Use the **Internal Database URL**, not the External
   Database URL, when the web service and database are in the same region.
4. Save the environment variable and deploy the web service. The API health
   endpoint is `/api/health`.

On its first database connection, the app creates its PostgreSQL tables and
sample records. Render's free PostgreSQL databases expire after 30 days; keep
the database on an active plan to avoid expiration and data deletion.

If the page reports `DB_CONNECTION_ERROR`, check the Render web-service logs,
confirm `DATABASE_URL` is present and copied in full, and verify that the
PostgreSQL database is available. The free web service may sleep when idle.

The web client starts with login and registration forms, validates sessions
through the PHP API, loads marketplace listings/categories from the database,
and submits listings through the API. Newly registered student accounts remain
pending until an administrator approves them; only approved students can post.
Posting an item requires a JPEG, PNG, or WebP photo (up to 5 MB). Uploaded files
are served from `web-learning/uploads/items`; on Render, use persistent storage
or external object storage if photos must survive service rebuilds and deploys.

---

## 🔑 Pre-Configured Test Accounts (Viva Presets)

These sample accounts are inserted when the app initializes an empty database:

| Role | Account Name | Email | Password | Status / Permissions |
|---|---|---|---|---|
| **Administrator** | Prof. Sharma | `admin@campus.edu` | `Admin@123` | Full admin privileges: approve/reject verifications, moderate listings, view audit logs. |
| **Verified Student 1** | Atharva Gokhale | `atharva@campus.edu` | `Student@123` | Approved student: can create listings, save wishlist, edit own items. |
| **Verified Student 2** | Janhavi Patil | `janhavi@campus.edu` | `Student@123` | Approved student: seller of Lab Coat & Hero Sprint bicycle. |
| **Verified Student 3** | Yash Deshmukh | `yash@campus.edu` | `Student@123` | Approved student: seller of engineering graphics tools & roller scales. |
| **Pending Student** | Rohit Kulkarni | `rohit@campus.edu` | `Student@123` | **Pending Approval**: demonstrating restriction enforcement (cannot create listings until approved by admin). |

---

## 🧪 Running Automated Tests

Run the comprehensive automated API test suite covering 18 integration scenarios:

```powershell
# In PowerShell:
C:\xampp\php\php.exe tests\api_smoke_test.php
```

**Test Coverage:**
- ✅ 1. Health check endpoint (200 OK)
- ✅ 2. Categories listing
- ✅ 3. Student authentication & session token issuance
- ✅ 4. Invalid password rejection (401 Unauthorized)
- ✅ 5. Admin authentication & role assignment
- ✅ 6. New student registration with default `pending` status
- ✅ 7. Duplicate email prevention (409 Conflict)
- ✅ 8. Role-based enforcement blocking unverified students from posting listings (403 Forbidden)
- ✅ 9. Administrator approval of pending student
- ✅ 10. Newly approved student posting live listing
- ✅ 11. Multi-parameter search & category filter queries
- ✅ 12. Ownership security check preventing editing another student's listing (403 Forbidden)
- ✅ 13. Owner updating their own listing price and condition
- ✅ 14. Wishlist add and duplicate check
- ✅ 15. Wishlist removal
- ✅ 16. Regular students blocked from accessing admin dashboard (403 Forbidden)
- ✅ 17. Administrator accessing system metrics and KPIs
- ✅ 18. Destructive deletion with cascading cleanup

---

## 🔬 Syllabus Module 1: JDBC Demonstration

To demonstrate raw Java Database Connectivity without the REST layer:

```powershell
# Double-click run-jdbc-demo.bat or run:
.\run-jdbc-demo.bat
```

Demonstrates:
- `Class.forName("com.mysql.cj.jdbc.Driver")`
- `DriverManager.getConnection(...)` with try-with-resources
- `Statement` and `ResultSet` for category querying
- `PreparedStatement` with auto-generated keys (`Statement.RETURN_GENERATED_KEYS`)
- Parameterized `SELECT`, `UPDATE`, and `DELETE`
- Transaction management with `setAutoCommit(false)`, in-memory mutation, and `rollback()`.

---

---

## 📁 Project Directory Structure

```
campus-marketplace/
├── backend-php/
│   ├── config/
│   │   ├── Config.php
│   │   └── Database.php
│   ├── public/
│   │   ├── index.php         (Front Controller & REST Router)
│   │   └── router.php        (Built-in Dev Server Router)
│   └── src/
│       ├── Controllers/      (Auth, Listing, Wishlist, Admin, Seller, Report)
│       ├── Middleware/       (AuthMiddleware, Verification Gate, Admin Gate)
│       └── Utils/            (Response, Validator)
├── database/
│   ├── schema.sql            (DDL table schemas, foreign keys, indexes)
│   ├── seed.sql              (MySQL sample data)
│   ├── schema.postgresql.sql (PostgreSQL DDL)
│   └── seed.postgresql.sql   (PostgreSQL sample data)
├── frontend-java/
│   ├── pom.xml               (Maven project configuration)
│   └── src/main/java/com/campus/marketplace/
│       ├── Main.java         (Entry point with FlatLaf initialization)
│       ├── config/           (AppConfig & Session Manager)
│       ├── models/           (User, Listing, Category, WishlistItem, Report, etc.)
│       ├── service/          (ApiClient, AuthService, MarketplaceService, AdminService)
│       ├── ui/
│       │   ├── ModernTheme.java
│       │   ├── MainFrame.java
│       │   ├── components/   (ProductCardPanel)
│       │   ├── dialogs/      (AddListingDialog, EditListingDialog, ProductDetailsDialog)
│       │   └── panels/       (Marketplace, Login, Register, Wishlist, Seller, Admin, etc.)
│       └── util/             (SwingWorkerHelper)
├── jdbc-demo/
│   ├── run-demo.bat
│   └── src/main/java/com/campus/jdbc/JdbcMarketplaceDemo.java
├── web-learning/
│   ├── index.html
│   ├── style.css
│   └── script.js
├── tests/
│   └── api_smoke_test.php    (Automated 18-step HTTP test suite)
├── docs/
│   └── PROJECT_REPORT.md     (Comprehensive Academic College Report)
├── run-app.bat               (1-Click Master Application Launcher)
├── start-backend.bat         (Backend starter)
├── run-frontend.bat          (Desktop client launcher)
├── run-jdbc-demo.bat         (JDBC demo launcher)
├── render-start.sh           (Render Docker web-service startup)
└── README.md
```
