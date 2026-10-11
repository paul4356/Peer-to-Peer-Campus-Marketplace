# Academic College Project Report

## Project Title:
**PEER-TO-PEER CAMPUS MARKETPLACE**
*A Three-Tier Distributed Academic Resource Trading Platform*

---

## 1. Abstract
University students routinely purchase expensive academic equipment—such as engineering textbooks, scientific calculators, workshop lab coats, graphics drafting tools, and campus bicycles—that are utilized for only one or two semesters. Upon semester completion, these items often remain unused, while incoming junior batches incur substantial financial costs purchasing the identical items at retail prices. 

The **Peer-to-Peer Campus Marketplace** addresses this inefficiency by creating a safe, collegiate-exclusive trading platform. Built on an enterprise 3-tier architecture, the system employs a modern Java Swing desktop interface for student and administrative interactions, communicating over HTTP REST APIs with a PHP 8+ microservice tier backed by a MySQL database. Mandatory administrator verification of student identities prevents spam and unauthorized commercial solicitations. The project further encompasses a syllabus-aligned JDBC direct-connectivity module and a client-side HTML5/CSS3/JavaScript laboratory.

---

## 2. Introduction & Problem Statement

### 2.1 Problem Statement
1. **Financial Burden on Students:** New engineering reference textbooks (e.g., B.S. Grewal, Beer & Johnston) and technical calculators (e.g., Casio fx-991EX) cost significant sums.
2. **Lack of Campus-Specific Platform:** Public e-commerce platforms (OLX, Quikr) do not cater to campus-specific needs, expose students to anonymous strangers, require shipping logistics, and introduce fraud risks.
3. **Identity Verification Deficit:** Open platforms lack verification of institutional student status, leading to spam and commercial vendors invading collegiate communities.
4. **Disorganized Communication:** Manual peer-to-peer trading via unstructured WhatsApp or Telegram groups leads to lost messages, lack of searchability, and zero accountability for dishonest transactions.

### 2.2 Objectives
- Develop an intuitive, modern desktop marketplace client using **Java Swing** with **FlatLaf** design system.
- Implement a decoupled, modular **PHP 8 REST API** enforcing role-based access control (RBAC), authentication tokens, and PDO prepared statements.
- Model a normalized, relational **MySQL database** adhering to BCNF/3NF principles, foreign keys, cascade rules, and security audit logs.
- Enforce strict **Student Verification** where newly registered students remain restricted from listing items until approved by an administrator.
- Build an academic **JDBC demonstration module** showcasing raw connection pooling, transactions, and CRUD operations.
- Provide a responsive **HTML5, CSS3, and JavaScript educational module** for client-side programming syllabus compliance.

---

## 3. Scope of the System
- **User Roles:**
  - **Unverified Student:** Can browse and search the marketplace catalog in read-only mode.
  - **Verified Student:** Can create new listings, manage own inventory, mark items as sold, save wishlist bookmarks, report abusive content, and update profile settings.
  - **Administrator:** Can view real-time platform KPIs, approve/reject student verifications with documented rationales, moderate and delete reported listings, and inspect immutable audit logs.
- **Academic Domain:** Standardized categories tailored to engineering and college campus life (Books, Scientific Calculators, Electronics, Lab Equipment, Bicycles, Lab Coats, and Engineering Graphics Stationery).

---

## 4. Software Requirements Specification (SRS)

### 4.1 Functional Requirements
1. **User Authentication & Authorization:**
   - Registration with institutional Roll Number, Department, and Year of Study.
   - Password encryption using industry-standard `bcrypt` (`PASSWORD_BCRYPT`).
   - Token-based stateless REST session maintenance.
2. **Student Verification Pipeline:**
   - Default assignment of `pending` verification status upon registration.
   - Admin authorization console displaying pending queue.
   - Immediate privilege escalation upon approval or documented rejection reason.
3. **Marketplace Catalog & Discovery:**
   - Multi-criteria filtering (Category, Condition: New/Like New/Good/Acceptable, Price Range).
   - Full-text search over listing titles and descriptions.
   - Sort criteria (Newest, Price ascending, Price descending, View count).
4. **Seller Inventory Management:**
   - Create, edit, and delete listings owned by the authenticated student.
   - One-click toggle between `available` and `sold` status.
   - Destructive action confirmation modals.
5. **Wishlist Persistence:**
   - Save and remove bookmarks in MySQL database without duplicates.
6. **Listing Moderation:**
   - Student reporting system for spam, duplicate items, or inappropriate content.
   - Administrative review queue with action history.

### 4.2 Non-Functional Requirements
- **Security:** Zero SQL injection vulnerability achieved via PDO parameterized prepared statements; prevention of privilege escalation via server-side role validation.
- **Responsiveness:** Asynchronous HTTP dispatching utilizing `SwingWorker` to prevent Event Dispatch Thread (EDT) freezing.
- **Extensibility:** Standardized JSON contracts allowing replacement or extension to web and mobile frontends.
- **Data Integrity:** Foreign key constraints (`ON DELETE CASCADE` / `RESTRICT`) and transactional rollback support.

---

## 5. Architectural Comparison (Viva Question Guide)

| Architectural Attribute | 2-Tier JDBC Architecture (Demo Module) | 3-Tier REST Architecture (Main System) |
|---|---|---|
| **Client Role** | Heavy client containing direct SQL queries. | Thin presentation client rendering JSON responses. |
| **Database Credentials** | Stored inside client application bytecode (Security risk). | Kept exclusively on backend server environment. |
| **Network Protocol** | Direct TCP/IP socket to MySQL port 3306. | Standard HTTP/HTTPS (Port 80/443/8000). |
| **Business Logic** | Embedded within client GUI classes. | Centralized in PHP Controllers and Middleware. |
| **Scalability** | Poor; each client holds persistent DB connections. | High; backend manages database connection pooling. |
| **Client Agnostic** | Only Java applications can connect directly. | Reusable across Swing, React, Android, and iOS. |

---

## 6. Database Schema Details

```
users (id, student_id, full_name, email, password_hash, phone, department, year_of_study, role, verification_status, rejection_reason, api_token, created_at)
categories (id, name, slug, description, icon_name, created_at)
listings (id, seller_id, category_id, title, description, price, condition_state, image_url, status, views_count, contact_preference, created_at)
wishlist (id, user_id, listing_id, created_at)
reports (id, reporter_id, listing_id, reason, details, status, admin_notes, created_at)
admin_audit_logs (id, admin_id, action, target_type, target_id, details, ip_address, created_at)
```

---

## 7. Testing & Verification Results

An automated regression smoke test (`tests/api_smoke_test.php`) validates the platform:

| Test ID | Test Scenario | Expected Status | Result |
|---|---|---|---|
| TC-01 | Health check endpoint | HTTP 200 | PASS |
| TC-02 | Category listing fetch | HTTP 200 | PASS |
| TC-03 | Valid student login | HTTP 200 (Token Issued) | PASS |
| TC-04 | Invalid password rejection | HTTP 401 Unauthorized | PASS |
| TC-05 | Admin login & role detection | HTTP 200 (Role: admin) | PASS |
| TC-06 | Student registration & default status | HTTP 201 (Status: pending) | PASS |
| TC-07 | Duplicate email prevention | HTTP 409 Conflict | PASS |
| TC-08 | Pending student listing restriction | HTTP 403 Forbidden | PASS |
| TC-09 | Administrator approval of student | HTTP 200 (Status: approved) | PASS |
| TC-10 | Approved student listing creation | HTTP 201 Created | PASS |
| TC-11 | Multi-parameter search & category filter | HTTP 200 OK | PASS |
| TC-12 | Ownership verification on listing update | HTTP 403 Forbidden | PASS |
| TC-13 | Owner listing update | HTTP 200 OK | PASS |
| TC-14 | Wishlist add & retrieval | HTTP 200 / 201 | PASS |
| TC-15 | Wishlist item removal | HTTP 200 OK | PASS |
| TC-16 | Student blocked from admin endpoint | HTTP 403 Forbidden | PASS |
| TC-17 | Admin retrieving system dashboard KPIs | HTTP 200 OK | PASS |
| TC-18 | Listing deletion cleanup | HTTP 200 OK | PASS |

**Overall Result: 18 / 18 Tests Passed (100% Success Rate)**

---

## 8. Limitations & Future Enhancements

### 8.1 Current Limitations
- Handover transactions occur physically between students on campus rather than online payment gateways.
- Product images currently utilize URL references and generated aesthetic SVG/canvas banners rather than multi-file uploads.

### 8.2 Future Enhancements
- Integration of razorpay/UPI campus escrow payments.
- Real-time WebSockets push chat between buyer and seller.
- Automated OCR extraction from uploaded college ID cards to verify enrollment automatically.

---

## 9. Conclusion
The **Peer-to-Peer Campus Marketplace** provides a practical, full-stack solution to campus resource sharing. Combining modern Java Swing desktop design with a secured PHP/MySQL REST architecture fulfills both academic rigor and real-world utility, demonstrating sound software engineering, database normalization, and distributed multi-tier paradigms.
