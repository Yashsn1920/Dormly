# Smart Hostel Complaint Management System: Backend Plan

Instructions for the coding agent. Read this whole file first, then work on **one stage at a time** when told which one.

---

## 0. How to work (read first)

1. Do **only** the stage you are told to do. Do not start later stages, do not "prepare" for them.
2. Each stage lists **Needs** (earlier stages that must exist), **Build**, and **Done when**. A stage is finished only when every "Done when" item is true.
3. Do not change decisions in Section 2 or the route contract in Appendix A. The UI stage will be built later against that contract, so it must stay stable. If something is genuinely impossible, stop and explain instead of silently deviating.
4. When a stage is finished, reply with: files created or changed, how to verify, and any deviation (ideally none).
5. Keep it a **mini project**: simple, readable, well-commented where logic is non-obvious. No over-engineering.

---

## 1. Hard rules

**Strictly Java.** Allowed dependencies, and nothing else:

| Purpose | Library |
|---|---|
| Web | Jakarta Servlet API 6 (scope `provided`) |
| Views | JSP + JSTL (`jakarta.servlet.jsp.jstl-api` + `org.glassfish.web:jakarta.servlet.jsp.jstl`) |
| Database | MySQL Connector/J (JDBC) |
| Tests | JUnit 5 (pure-logic classes only, no Mockito) |

Everything else comes from the JDK itself: `java.sql`, `java.time`, `java.util.logging`, `javax.crypto` (password hashing), `javax.imageio` (image validation), `java.nio.file`.

**Forbidden:** Spring, Hibernate/JPA, Lombok, Jackson/Gson, any ORM, any connection-pool library, any JS framework, any AI/LLM code, any "agent" features.

**Versions:** Java 17, Maven (WAR packaging), Tomcat 10.1 (so the package namespace is `jakarta.servlet.*`, not `javax.servlet.*`), MySQL 8.

**OOP rules (this is a viva-graded project, so these matter):**

- Private fields, public getters/setters, constructors that enforce valid state. Add `toString()`, and `equals()`/`hashCode()` where useful.
- No SQL outside DAO classes. No business rules in servlets or JSPs. Servlets only: read request, call a service, set attributes, forward or redirect.
- Program to interfaces: DAOs are interfaces with JDBC implementations.
- Enums carry behaviour (display names, SLA days, allowed transitions), not just constants.
- Custom exception hierarchy; never swallow exceptions; never print stack traces to the user.
- Always `try-with-resources` for JDBC objects. Always `PreparedStatement`.
- `java.time` (`LocalDateTime`) everywhere; convert to `Timestamp` only inside DAOs.

### OOP concept map (keep this accurate, it is your viva cheat sheet)

| Concept | Where it is shown |
|---|---|
| **Encapsulation** | Every model class (private fields + getters/setters); `Complaint` guards its own state |
| **Inheritance** | `User` (abstract) → `Student`, `Warden`; `BaseServlet` → all servlets; `HostelException` → specific exceptions |
| **Polymorphism** | `User.getDashboardPath()` overridden per subclass; `PriorityPolicy` implementations; DAOs used through interfaces |
| **Abstraction** | Abstract `User`, abstract `BaseServlet`, DAO interfaces, `PriorityPolicy` interface |
| **Enums with behaviour** | `Priority.getSlaDays()`, `ComplaintStatus.canTransitionTo()` |
| **Composition** | `Complaint` has a `Student`; `ComplaintService` has DAOs; `ComplaintUpdate` timeline held by `Complaint` view data |
| **Exception handling** | `HostelException` hierarchy mapped to user messages in `BaseServlet` |
| **Design patterns (light)** | DAO, Service layer, Strategy (`PriorityPolicy`), Singleton (`DBConnection` config), Factory method (`UserFactory` building `Student`/`Warden` from a DB row) |

---

## 2. Locked decisions

- **Roles:** `STUDENT`, `WARDEN`. Warden also does admin work. No separate admin or staff login.
- **Warden account** is never registered via the UI. It is auto-created on first startup if none exists (Stage 4), using credentials from `app.properties`.
- **Student registration** creates a `PENDING` account. Student cannot log in until the warden approves (`ACTIVE`). Warden can later deactivate (`INACTIVE`).
- **Passwords:** PBKDF2 (`PBKDF2WithHmacSHA256`, 65,536 iterations, 16-byte random salt via `SecureRandom`) using `javax.crypto`. Stored as `base64(salt):base64(hash)` in one column. Compare in constant time (`MessageDigest.isEqual`).
- **DB connections:** a simple `DBConnection.getConnection()` using `DriverManager`, config read once from `app.properties`. No pool. Fine at this scale.
- **Views:** JSPs live under `WEB-INF/views/` so they can only be reached through servlets. Servlets forward to them. Use Post-Redirect-Get after every successful POST.
- **Messages:** one-shot "flash" message stored in session (`FlashMessage`), consumed on next render.
- **No JavaScript needed by the backend.** Duplicate warning is handled server-side (see Stage 5).
- **No pagination, no email/SMS, no scheduler.** Overdue is computed on the fly.

### Complaint status flow

```
SUBMITTED → IN_PROGRESS → RESOLVED → CLOSED        (student confirms)
                              ↓
                           REOPENED → IN_PROGRESS   (student reopens, remark required)
SUBMITTED → REJECTED                                (warden, reason required)
```

Who may trigger what:
- **Warden:** `SUBMITTED→IN_PROGRESS`, `SUBMITTED→REJECTED`, `IN_PROGRESS→RESOLVED`, `REOPENED→IN_PROGRESS`
- **Student (owner only):** `RESOLVED→CLOSED`, `RESOLVED→REOPENED`
- Feedback (rating 1–5 + optional comment) only on a `CLOSED` complaint, **once**, by the owner.
- Open statuses (count towards overdue): `SUBMITTED`, `IN_PROGRESS`, `REOPENED`.

### Smart features (exact rules)

| Feature | Rule |
|---|---|
| **Auto-priority** | Electrical, Plumbing/Water, Security → `HIGH`; Cleaning, Furniture → `MEDIUM`; Wi-Fi, Mess/Food, Other → `LOW`. Set at creation by `PriorityPolicy`. Student cannot choose it. |
| **Overdue** | SLA from `created_at`: HIGH = 1 day, MEDIUM = 3 days, LOW = 5 days. Overdue if status is open and `now > created_at + SLA`. |
| **Duplicate check** | Before saving: if any **open** complaint exists from the **same block + room** in the **same category**, warn the student; they may still submit by confirming. |
| **Feedback analytics** | Average rating and count per category, shown to the warden in reports. |

---

## 3. Project structure

```
hostel-complaint-system/
├─ pom.xml
├─ README.md
├─ sql/
│   └─ schema.sql
├─ src/main/java/com/hostel/
│   ├─ model/
│   │   ├─ User.java              (abstract, Serializable)
│   │   ├─ Student.java
│   │   ├─ Warden.java
│   │   ├─ Complaint.java
│   │   ├─ ComplaintUpdate.java
│   │   ├─ Notice.java
│   │   └─ enums/  Role, UserStatus, Category, Priority, ComplaintStatus
│   ├─ exception/
│   │   ├─ HostelException.java        (base, extends RuntimeException)
│   │   ├─ DataAccessException.java
│   │   ├─ ValidationException.java    (carries Map<field, message>)
│   │   ├─ AuthenticationException.java
│   │   ├─ AccessDeniedException.java
│   │   ├─ BusinessRuleException.java
│   │   └─ NotFoundException.java
│   ├─ dao/            UserDAO, ComplaintDAO, NoticeDAO  (interfaces)
│   │   └─ impl/       UserDAOImpl, ComplaintDAOImpl, NoticeDAOImpl, UserFactory
│   ├─ service/
│   │   ├─ AuthService.java
│   │   ├─ UserService.java            (warden: manage students)
│   │   ├─ ComplaintService.java
│   │   ├─ NoticeService.java
│   │   ├─ ReportService.java
│   │   ├─ PriorityPolicy.java         (interface)
│   │   └─ CategoryPriorityPolicy.java (implementation)
│   ├─ report/         DashboardStats, CategoryStat, MonthlyStat, MonthlyReport, ComplaintFilter
│   ├─ controller/
│   │   ├─ BaseServlet.java            (abstract)
│   │   ├─ LoginServlet, RegisterServlet, LogoutServlet, HomeServlet
│   │   ├─ ProfileServlet
│   │   ├─ StudentDashboardServlet, StudentComplaintServlet, StudentNoticeServlet
│   │   ├─ WardenDashboardServlet, WardenComplaintServlet, WardenStudentServlet,
│   │   │  WardenNoticeServlet, ReportServlet
│   │   └─ FileServlet.java            (serves complaint images, access-checked)
│   ├─ filter/         EncodingFilter, AuthFilter
│   ├─ listener/       AppStartupListener  (creates upload dir + default warden)
│   └─ util/           DBConnection, PasswordUtil, Validator, FlashMessage, FileStorage
├─ src/main/resources/
│   └─ app.properties              (db url/user/password, upload.dir, default warden)
├─ src/main/webapp/
│   ├─ WEB-INF/
│   │   ├─ web.xml
│   │   └─ views/
│   │       ├─ auth/      login.jsp, register.jsp
│   │       ├─ student/   dashboard, raise-complaint, my-complaints, complaint-detail, notices, profile
│   │       ├─ warden/    dashboard, complaints, update-complaint, students, notices, reports
│   │       ├─ error.jsp
│   │       └─ access-denied.jsp
│   └─ static/            (empty; reserved for the UI stage: css, js, img)
└─ src/test/java/com/hostel/   (JUnit tests for pure logic)
```

Changes from the original context doc: added `service` layer (keeps servlets thin, more OOP to show), `exception` and `report` packages, `listener`, `enums`, an abstract `BaseServlet`, and moved JSPs into `WEB-INF/views` for security. Dropped `filter`-only role logic from servlets, and `PriorityUtil` became a proper `PriorityPolicy` Strategy.

---

## 4. Database schema (final)

Create exactly this in `sql/schema.sql` (plus `CREATE DATABASE hostel_db` + `USE`):

```sql
CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  usn           VARCHAR(20)  UNIQUE,              -- NULL for warden
  email         VARCHAR(120) NOT NULL UNIQUE,
  phone         VARCHAR(15)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(10)  NOT NULL,            -- STUDENT | WARDEN
  block         VARCHAR(10),                      -- NULL for warden
  room_no       VARCHAR(10),
  status        VARCHAR(10)  NOT NULL DEFAULT 'PENDING', -- PENDING | ACTIVE | INACTIVE
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  student_id   INT          NOT NULL,
  category     VARCHAR(20)  NOT NULL,
  title        VARCHAR(150) NOT NULL,
  description  TEXT         NOT NULL,
  priority     VARCHAR(10)  NOT NULL,
  status       VARCHAR(15)  NOT NULL DEFAULT 'SUBMITTED',
  image_path   VARCHAR(255),
  assigned_to  VARCHAR(100),
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at  TIMESTAMP    NULL,
  rating       TINYINT      NULL,
  feedback     VARCHAR(500) NULL,
  CONSTRAINT fk_c_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT chk_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5),
  INDEX idx_c_status (status),
  INDEX idx_c_category (category),
  INDEX idx_c_student (student_id)
);

CREATE TABLE complaint_updates (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  complaint_id INT          NOT NULL,
  status       VARCHAR(15)  NOT NULL,
  remark       VARCHAR(500),
  updated_by   INT          NOT NULL,
  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_u_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  CONSTRAINT fk_u_user FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE TABLE notices (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  title      VARCHAR(150) NOT NULL,
  message    TEXT         NOT NULL,
  posted_by  INT          NOT NULL,
  posted_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_n_user FOREIGN KEY (posted_by) REFERENCES users(id)
);
```

Notes: `resolved_at` is set when a complaint becomes `RESOLVED` and cleared when it is `REOPENED`. Enum values are stored as the Java `name()` strings. A "room" is identified by `block + room_no` from the student's profile.

---

## 5. Stages

### Stage 0: Project setup

**Needs:** nothing.

**Build:**
- Maven project (`war`, Java 17) with the dependencies from Section 1. Servlet API `provided`; MySQL, JSTL compile scope; JUnit test scope.
- The full package/folder skeleton from Section 3 (empty packages may hold a `package-info.java` or placeholder class, so Git keeps them).
- `sql/schema.sql` from Section 4.
- `app.properties`: `db.url`, `db.user`, `db.password`, `upload.dir` (e.g. `${user.home}/hostel-uploads`), `warden.email`, `warden.password`, `warden.name`.
- `util/DBConnection` (Singleton-style config loader + `getConnection()`).
- `EncodingFilter` (UTF-8 request/response) and a `web.xml` with: session timeout 30 min, `welcome-file` pointing to `/`, error pages for 404, 500 and `java.lang.Throwable` → `error.jsp`, and an `access-denied.jsp` for 403.
- A minimal `HomeServlet` mapped to `/` that for now just prints "OK" to confirm deployment.
- `README.md` with local setup steps (create DB, edit `app.properties`, build, deploy to Tomcat).

**Done when:**
- The WAR builds: `mvn clean package 2>&1 | tee >(wl-copy)`
- Deployed on Tomcat 10.1, `/` responds.
- `DBConnection.getConnection()` connects to MySQL (a startup log line confirms it).
- Schema script runs cleanly on an empty MySQL database.

---

### Stage 1: Domain model

**Needs:** Stage 0.

**Build:**
- Enums in `model/enums`:
  - `Role`, `UserStatus`
  - `Category` (display name each: "Electrical", "Plumbing/Water", "Cleaning", "Furniture", "Wi-Fi", "Mess/Food", "Security", "Other")
  - `Priority` (HIGH/MEDIUM/LOW, each with `getSlaDays()` and display name)
  - `ComplaintStatus` (display name, `isOpen()`, `canTransitionTo(ComplaintStatus next)` implementing the flow in Section 2)
- `User` (abstract, `Serializable`): id, name, email, phone, passwordHash, role, status, createdAt; abstract `getDashboardPath()`.
- `Student extends User`: usn, block, roomNo; `getRoomLabel()` returns e.g. "A-204". `getDashboardPath()` → `/student/dashboard`.
- `Warden extends User`: `getDashboardPath()` → `/warden/dashboard`.
- `Complaint`: all columns as fields, plus a composed `Student student` and a `List<ComplaintUpdate> timeline`. Methods: `isOverdue(LocalDateTime now)`, `canBeConfirmedByStudent()`, `canBeReopenedByStudent()`, `canBeRated()`, `getDueAt()`.
- `ComplaintUpdate`, `Notice` (plain encapsulated classes; `ComplaintUpdate` also carries `updatedByName` for display).
- The whole `exception` package.
- `report` package DTOs: `DashboardStats` (total, pending, inProgress, resolved, rejected, overdue), `CategoryStat` (category, count, avgRating, ratingCount, avgResolutionHours), `MonthlyStat` (yearMonth, count), `MonthlyReport`, and `ComplaintFilter` (all optional: status, category, priority, block, roomNo, fromDate, toDate, keyword, studentId).
- `PriorityPolicy` interface + `CategoryPriorityPolicy` (the mapping in Section 2).
- JUnit tests: every legal and illegal status transition; `Category → Priority` mapping; `isOverdue` at boundary times for each priority and for closed/rejected complaints (never overdue); `getRoomLabel`.

**Done when:** all tests pass, no class has public fields, and `Complaint` rejects obviously invalid state (null category, rating outside 1–5) with `ValidationException` or `IllegalArgumentException`.

---

### Stage 2: Utilities and DAO layer

**Needs:** Stages 0–1.

**Build:**
- `PasswordUtil`: `hash(char[]/String)` and `verify(String raw, String stored)` per Section 2. JUnit test: same password hashes differently each time, verify works, wrong password fails.
- `Validator`: small reusable checks (required, max length, email format, phone = 10 digits, USN pattern, password min 8 chars with letter and digit). Collects errors into a `Map<String,String>` and throws `ValidationException`.
- DAO interfaces and JDBC implementations. Wrap every `SQLException` in `DataAccessException`.
  - `UserDAO`: `insert`, `findById`, `findByEmail`, `findByUsn`, `findByStatus(role, status)`, `updateStatus`, `updateProfile`, `updatePassword`, `existsWardenAccount()`.
  - `UserFactory`: builds `Student` or `Warden` from a `ResultSet` row based on `role`.
  - `ComplaintDAO`: `insert` (also inserts the first `complaint_updates` row), `findById` (with student joined), `findByFilter(ComplaintFilter)` (dynamic `WHERE` built with a parameter list, still 100% `PreparedStatement`), `findOpenByRoomAndCategory(block, roomNo, category)`, `updateStatus(complaintId, newStatus, remark, actorId)` which updates `complaints` **and** inserts a `complaint_updates` row **in one transaction** (also sets/clears `resolved_at`), `updateAssignedTo`, `saveFeedback(id, rating, comment)`, `findTimeline(id)`, `countByStatusForStudent(studentId)`, `findOpenComplaints()` (for overdue calculation), `countByCategory()`, `avgRatingByCategory()`, `avgResolutionHoursByCategory()`, `countByMonth(from, to)`.
  - `NoticeDAO`: `insert`, `findAll` (newest first), `findLatest(n)`, `delete`.

**Done when:**
- A throwaway `main` or test harness (delete it afterwards, or keep it under `src/test` clearly named) inserts a student and a complaint, reads them back, changes status, and shows the timeline rows.
- Grep confirms no SQL string exists outside `dao/impl`.
- Transaction check: if the timeline insert fails, the status update rolls back.

---

### Stage 3: Service layer (all business rules)

**Needs:** Stages 0–2.

**Build:** services receive DAOs through their constructors (plain constructor injection; no framework).

- `AuthService`
  - `register(...)`: validate, reject duplicate email/USN, hash password, save as `PENDING` `Student`.
  - `login(email, password)`: generic "Invalid email or password" for unknown email or wrong password; separate clear messages for `PENDING` ("awaiting warden approval") and `INACTIVE` ("account deactivated").
  - `changePassword(userId, current, new, confirm)`, `updateProfile(...)`.
- `UserService` (warden use): `listStudents(status)`, `approve`, `deactivate`, `reactivate`, with the rule that only a `PENDING` student can be approved.
- `ComplaintService`
  - `raiseComplaint(student, category, title, description, imagePath, confirmDuplicate)`: validate; call `PriorityPolicy`; run the duplicate check and, if a duplicate exists and `confirmDuplicate` is false, throw a dedicated `DuplicateComplaintException` (a `BusinessRuleException` subclass that carries the existing complaint); otherwise save.
  - `getForStudent(complaintId, student)`: owner check, throws `AccessDeniedException` otherwise. `getForWarden(id)`.
  - `listForStudent(studentId, status)`, `search(ComplaintFilter)` for the warden.
  - `wardenUpdateStatus(id, newStatus, remark, warden)`, `reject(id, reason, warden)` (reason required), `assign(id, assignedTo, warden)`.
  - `studentConfirm(id, student)`, `studentReopen(id, remark, student)` (remark required).
  - `submitFeedback(id, rating, comment, student)`: only for owner, only when `CLOSED`, only once.
  - Every transition is checked with `ComplaintStatus.canTransitionTo()` **and** the "who may trigger" table in Section 2; illegal → `BusinessRuleException`.
  - `studentStats(studentId)` and `wardenStats()` returning `DashboardStats` (overdue is counted in Java using `Complaint.isOverdue`); `overdueComplaints(limit)`; `categoryCounts()`.
  - Student "Pending" = `SUBMITTED + REOPENED`; "Resolved" = `RESOLVED + CLOSED`.
- `NoticeService`: `post` (validated), `delete`, `listAll`, `latest(n)`.

**Done when:** a JUnit test (pure logic only, no DB) covers `PriorityPolicy` use and the transition-permission rules if they are extractable into a small class (e.g. `TransitionRules`); a manual harness run shows: duplicate warning fires, wrong-owner access is refused, a student cannot close an `IN_PROGRESS` complaint, feedback twice is refused.

---

### Stage 4: Web foundation and authentication

**Needs:** Stages 0–3.

**Build:**
- `BaseServlet` (abstract, extends `HttpServlet`): `getCurrentUser(req)`, `getStudent(req)` (casts safely), `forward(req, resp, view)`, `redirect(req, resp, path)` (adds context path), `setFlash/consumeFlash`, `parseIntParam(req, name)`, `trim(req, name)`, and a central `handle(HostelException)` that maps exception type → flash message / error page / 403 / 404.
- `FlashMessage` util (type: success|error|info + text, stored in session, removed after being read into the request attribute `flash`).
- `AuthFilter`: `/student/*` needs a logged-in active `STUDENT`; `/warden/*` needs a logged-in active `WARDEN`; not logged in → redirect `/login`; wrong role → 403 `access-denied.jsp`. Also sends no-cache headers on protected pages so the Back button after logout shows nothing.
- `LoginServlet` (GET form, POST authenticate; on success **invalidate the old session and create a new one**, store the `User`, redirect to `user.getDashboardPath()`), `LogoutServlet` (POST, invalidates session), `RegisterServlet`, `HomeServlet` (redirect to login or the role's dashboard), `ProfileServlet` (view, update profile, change password; shared by both roles but the view differs by role).
- `AppStartupListener`: creates the upload directory; if no warden exists, creates one from `app.properties` (hashed password), and logs a reminder to change the default password.
- Bare-bones placeholder JSPs for login, register, student/warden dashboard (just prints `currentUser`), student/warden profile, `error.jsp`, `access-denied.jsp`. **Plain unstyled HTML only**; the UI stage replaces them.

**Done when (manual test script):**
1. Register a student → cannot log in ("awaiting approval").
2. Log in as the seeded warden → reaches warden dashboard.
3. (Approval screen comes in Stage 6; for now approve via SQL) → student logs in and reaches the student dashboard.
4. Student opening `/warden/dashboard` gets 403; logged-out user opening `/student/dashboard` goes to login.
5. After logout, pressing Back does not show protected content.
6. Wrong password and unknown email show the same message.

---

### Stage 5: Student features

**Needs:** Stages 0–4.

**Build** (routes, form fields and request attributes are exactly as in Appendix A):
- `StudentDashboardServlet`: `stats`, `recent` (last 5 complaints), `latestNotices` (3).
- `StudentComplaintServlet`: new-complaint form, create (multipart), list with status filter, detail with timeline, confirm, reopen, feedback.
- `FileStorage` util: `@MultipartConfig` upload, max **2 MB**, only `.jpg/.jpeg/.png`, verified with `ImageIO.read` (reject if it is not a real image), saved as `UUID + extension` inside `upload.dir`, returns the stored filename. Image is optional.
- `FileServlet` (`/files/complaint?id=`): streams the image only to the owner or the warden; never exposes the raw file path.
- `StudentNoticeServlet`: list notices.
- **Duplicate flow without JavaScript:** on `POST /student/complaints`, if `DuplicateComplaintException` is thrown, re-show the form with the entered values (`form`), the existing complaint as `duplicateWarning`, and a checkbox/hidden field `confirmDuplicate=true` the student submits again to proceed.
- Auto-fill room: read from the logged-in student's profile, never trust a room value from the form.
- Placeholder JSPs for all student pages: plain tables and forms, unstyled.

**Done when (manual test script):**
1. Raise a Plumbing complaint → priority shows HIGH, timeline has one "SUBMITTED" row.
2. Raise another Plumbing complaint from the same room → duplicate warning appears; submitting with confirmation succeeds.
3. Upload a `.txt` renamed to `.jpg` → rejected. Upload a 5 MB image → rejected.
4. Student A cannot open Student B's complaint or image (403/404).
5. With status set to `RESOLVED` via SQL: confirm → `CLOSED`; reopen without remark → validation error; rate once OK, twice refused.
6. Status filter on "My Complaints" works.

---

### Stage 6: Warden features

**Needs:** Stages 0–5.

**Build** (per Appendix A):
- `WardenDashboardServlet`: `stats` (incl. overdue), `categoryCounts`, `overdueComplaints` (top 10), `pendingStudentCount`.
- `WardenComplaintServlet`: filterable list, detail page with timeline and `allowedNextStatuses`, update status + remark, reject with reason, assign (free-text staff type or name).
- `WardenStudentServlet`: list by status, approve, deactivate, reactivate.
- `WardenNoticeServlet`: list, post, delete.
- Placeholder JSPs for all warden pages (plain, unstyled).

**Done when (manual test script):**
1. A new student appears under Pending; approving lets them log in; deactivating blocks login again.
2. Search by each filter (status, category, priority, block, room, date range, keyword) and with several combined.
3. Move a complaint `SUBMITTED → IN_PROGRESS → RESOLVED` with remarks; the student's timeline shows each step with the warden's remark.
4. Illegal moves (e.g. `SUBMITTED → RESOLVED`, or touching a `CLOSED` complaint) are refused with a clear message.
5. Reject without a reason is refused.
6. Make a HIGH complaint older than 1 day (edit `created_at` in SQL) → it appears as overdue on the dashboard and its count is correct.
7. A notice posted by the warden shows up on the student's notices page and can be deleted.

---

### Stage 7: Reports and feedback analytics

**Needs:** Stages 0–6.

**Build:**
- `ReportService`: for a chosen month (`yyyy-MM`, default current): total complaints, by status, by category; **average resolution time** (hours, from `created_at` to `resolved_at`); **average rating**; per-category average rating and rating count (the feedback analytics feature); complaints per month for the last 6 months.
- `ReportServlet` (`/warden/reports`) setting the attributes in Appendix A.
- Placeholder `reports.jsp` as plain tables.
- Handle empty data gracefully (no division by zero; averages are `null` or `0` clearly shown as "no data").

**Done when:** numbers on the report match hand-counted SQL queries for a seeded dataset; an empty month renders without errors; the weakest-rated category is obvious from the table.

---

### Stage 8: Hardening, demo data, handover

**Needs:** Stages 0–7.

**Build:**
- `DemoDataSeeder` (a `main` class under `src/test` or a clearly named utility): creates ~10 students, ~40 complaints across categories, statuses, dates and ratings, so reports and overdue look realistic in the demo.
- Security pass: confirm `PreparedStatement` everywhere; every POST re-checks ownership/role in the **service**, not just the filter; HTML-escape all user text in the placeholder JSPs (`<c:out>`); no stack traces shown to users; login error messages don't leak whether an email exists.
- Run the full manual scripts of Stages 4–7 again end to end.
- Clean up: remove dead code and the throwaway harness, make sure comments match the code.
- Finish `README.md`: prerequisites, DB setup, config, build and deploy, default warden login, how to run the demo seeder, package overview, and a short "OOP concepts used and where" section copied from Section 1.

**Done when:** a fresh clone can be set up from the README alone, the demo flow (register → approve → raise → duplicate warning → progress → resolve → confirm → rate → report) works with no errors, and the OOP concept map in Section 1 is still accurate.

---

## Appendix A: Route and view contract

This is what the UI stage will build against. Do not rename anything here.

**Available in every view:** `currentUser` (the `User`), `flash` (one-shot `FlashMessage`, may be null). Use `${pageContext.request.contextPath}` for links. JSP paths are relative to `/WEB-INF/views/`. Every POST form is followed by a redirect (PRG). On validation failure the servlet forwards back to the form with `errors` (`Map<String,String>`, field → message) and `form` (`Map<String,String>`, the values entered).

### Public

| URL | Method | Form fields | Request attributes | Result |
|---|---|---|---|---|
| `/` | GET | | | Redirect to `/login` or the role's dashboard |
| `/login` | GET | | | `auth/login.jsp` |
| `/login` | POST | `email`, `password` | on failure: `error`, `email` | Redirect to dashboard, or forward `auth/login.jsp` |
| `/register` | GET | | | `auth/register.jsp` |
| `/register` | POST | `name`, `usn`, `email`, `phone`, `block`, `roomNo`, `password`, `confirmPassword` | on failure: `errors`, `form` | Redirect `/login` with flash, or forward `auth/register.jsp` |
| `/logout` | POST | | | Redirect `/login` |

### Student (`ROLE = STUDENT`)

| URL | Method | Form fields | Request attributes | View / result |
|---|---|---|---|---|
| `/student/dashboard` | GET | | `stats` (`DashboardStats`), `recent` (`List<Complaint>`), `latestNotices` (`List<Notice>`) | `student/dashboard.jsp` |
| `/student/complaints/new` | GET | | `categories` (`Category[]`) | `student/raise-complaint.jsp` |
| `/student/complaints` | POST (multipart) | `category`, `title`, `description`, `image` (optional file), `confirmDuplicate` (`true` only on re-submit) | on failure: `errors`, `form`, `categories`; on duplicate: also `duplicateWarning` (`Complaint`) | Redirect to detail, or forward `student/raise-complaint.jsp` |
| `/student/complaints` | GET | query `status` (optional) | `complaints`, `statuses` (`ComplaintStatus[]`), `selectedStatus` | `student/my-complaints.jsp` |
| `/student/complaints/view` | GET | query `id` | `complaint`, `timeline` (`List<ComplaintUpdate>`), `canConfirm`, `canReopen`, `canRate` (booleans), `overdue` (boolean) | `student/complaint-detail.jsp` |
| `/student/complaints/confirm` | POST | `id` | | Redirect to detail |
| `/student/complaints/reopen` | POST | `id`, `remark` (required) | | Redirect to detail |
| `/student/complaints/feedback` | POST | `id`, `rating` (1–5), `comment` | | Redirect to detail |
| `/student/notices` | GET | | `notices` | `student/notices.jsp` |
| `/student/profile` | GET | | `user` | `student/profile.jsp` |
| `/student/profile` | POST | `name`, `phone`, `block`, `roomNo` | on failure: `errors`, `form` | Redirect to profile |
| `/student/profile/password` | POST | `currentPassword`, `newPassword`, `confirmPassword` | on failure: `errors` | Redirect to profile |
| `/files/complaint` | GET | query `id` | | Streams the image (owner or warden only) |

### Warden (`ROLE = WARDEN`)

| URL | Method | Form fields | Request attributes | View / result |
|---|---|---|---|---|
| `/warden/dashboard` | GET | | `stats` (incl. `overdue`), `categoryCounts` (`List<CategoryStat>`), `overdueComplaints`, `pendingStudentCount` | `warden/dashboard.jsp` |
| `/warden/complaints` | GET | query `status`, `category`, `priority`, `block`, `roomNo`, `from`, `to` (`yyyy-MM-dd`), `q` | `complaints`, `filter` (`ComplaintFilter`), `categories`, `statuses`, `priorities` | `warden/complaints.jsp` |
| `/warden/complaints/view` | GET | query `id` | `complaint`, `timeline`, `allowedNextStatuses` (`List<ComplaintStatus>`), `overdue` | `warden/update-complaint.jsp` |
| `/warden/complaints/update` | POST | `id`, `status`, `remark` | | Redirect to detail |
| `/warden/complaints/reject` | POST | `id`, `reason` (required) | | Redirect to detail |
| `/warden/complaints/assign` | POST | `id`, `assignedTo` | | Redirect to detail |
| `/warden/students` | GET | query `status` (default `PENDING`) | `students` (`List<Student>`), `selectedStatus` | `warden/students.jsp` |
| `/warden/students/approve` | POST | `id` | | Redirect to students |
| `/warden/students/deactivate` | POST | `id` | | Redirect to students |
| `/warden/students/reactivate` | POST | `id` | | Redirect to students |
| `/warden/notices` | GET | | `notices` | `warden/notices.jsp` |
| `/warden/notices` | POST | `title`, `message` | on failure: `errors`, `form` | Redirect to notices |
| `/warden/notices/delete` | POST | `id` | | Redirect to notices |
| `/warden/reports` | GET | query `month` (`yyyy-MM`) | `selectedMonth`, `report` (`MonthlyReport`), `monthlyCounts` (`List<MonthlyStat>`), `categoryStats` (`List<CategoryStat>` with count, avgRating, avgResolutionHours), `overallAvgRating`, `overallAvgResolutionHours` | `warden/reports.jsp` |
| `/warden/profile` | GET/POST | same as student profile, minus `block`/`roomNo` | | `warden/profile.jsp` |

---

## Appendix B: Prompt template for each stage

Paste this when you start a stage, changing the stage number:

> Read `BACKEND_PLAN.md` fully. Do **Stage N only**, exactly as written, obeying Section 1 (hard rules) and Section 2 (locked decisions). Do not start any other stage. When finished, list the files you created or changed, how I can verify the "Done when" items, and any deviation from the plan. If something in the plan is impossible or ambiguous, stop and ask before improvising.
