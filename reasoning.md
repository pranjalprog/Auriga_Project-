# Technical Reasoning & Engineering Decisions

## 1. Project Overview

This project is a clinic appointment booking system designed to provide a reliable way to register users, authenticate them, search appointments, book appointments, manage appointment states, and persist all important data in a relational database.

The main engineering goals were:

* Reliable data persistence
* Clear separation between UI, API, business logic, and database
* Easy validation and maintenance
* RESTful API design
* Secure user authentication
* Search, pagination, and sorting
* Deterministic testing of time-dependent appointment behaviour
* A simple and usable user interface

The architecture was designed around:

```text
User
  ↓
UI
  ↓
REST Controller
  ↓
Service Layer
  ↓
Repository
  ↓
PostgreSQL
```

---

# 2. Why Java?

Java was selected as the primary backend language because it is:

* Strongly typed
* Mature and stable
* Widely used for enterprise backend systems
* Well suited for REST APIs
* Supported by a large ecosystem
* Easy to structure using object-oriented design
* Well supported by testing and database frameworks

For a system such as appointment management, strong typing is useful because entities such as `User`, `Doctor`, `Patient`, and `Appointment` have clearly defined fields and relationships.

Java also provides good support for handling date and time using classes such as:

```java
LocalDateTime
LocalDate
LocalTime
```

This is particularly useful for appointment scheduling.

---

# 3. Why Spring Boot?

Spring Boot was selected because it provides a mature framework for building REST APIs and database-backed applications.

It reduces the amount of boilerplate configuration required while still allowing the application to be structured into separate layers.

The project uses the following Spring concepts:

```text
Spring Boot
    │
    ├── Spring Web
    │      └── REST Controllers
    │
    ├── Spring Data JPA
    │      └── Repository / Database Access
    │
    ├── Spring Security
    │      └── Authentication / Authorization
    │
    └── Dependency Injection
           └── Services and Components
```

### Why this structure?

Instead of putting database queries, validation, and business logic directly inside controllers, responsibilities are separated.

For example:

```text
AppointmentController
        ↓
AppointmentService
        ↓
AppointmentRepository
        ↓
PostgreSQL
```

This makes the code easier to test and modify.

---

# 4. Why PostgreSQL?

PostgreSQL was selected as the primary database because the application contains **structured relational data**.

The system naturally contains relationships such as:

```text
User
  │
  ├── Patient
  │
  └── Doctor

Doctor
  │
  └── Appointments
         │
         └── Patient
```

A relational database is a natural fit for this structure.

### Important reasons for choosing PostgreSQL

#### 4.1 Relational data

Appointments have relationships with doctors and patients.

For example:

```text
Appointment
 ├── doctor_id
 └── patient_id
```

These relationships can be represented using foreign keys.

#### 4.2 Data consistency

PostgreSQL provides:

* Primary keys
* Foreign keys
* Unique constraints
* Transactions
* Data types
* Referential integrity

These are important for appointment systems because incorrect relationships or duplicate records can cause real application problems.

#### 4.3 Date and time support

Appointment systems depend heavily on date/time values.

PostgreSQL provides suitable timestamp/date types that work well with Java's date/time API.

#### 4.4 Search, sorting and pagination

The application needs to search and retrieve appointment records efficiently.

Relational databases are well suited for:

```sql
WHERE
ORDER BY
LIMIT
OFFSET
```

and can use indexes as the dataset grows.

#### 4.5 Production suitability

PostgreSQL is widely used for production applications and provides features beyond the requirements of this assessment.

---

# 5. Why Not an In-Memory Database?

An in-memory database such as H2 could have been used for a simple demonstration, but it would not satisfy the goal of demonstrating **real persistence** as effectively.

With an in-memory database:

```text
Application starts
      ↓
Data exists
      ↓
Application stops
      ↓
Data is lost
```

With PostgreSQL:

```text
Application
      ↓
PostgreSQL
      ↓
Data remains stored
      ↓
Application restarts
      ↓
Data can still be retrieved
```

For an appointment management system, persistent data is essential because appointments should not disappear when the application restarts.

---

# 6. Why JPA / Hibernate?

JPA with Hibernate was selected to map Java objects to relational database tables.

For example:

```java
Appointment
```

can represent a database table:

```text
appointments
```

and fields such as:

```text
id
doctor
patient
startTime
endTime
status
```

can be mapped to database columns.

This avoids writing raw SQL for every basic CRUD operation.

The general flow becomes:

```text
Java Entity
     ↓
JPA / Hibernate
     ↓
SQL
     ↓
PostgreSQL
```

This also makes relationships easier to represent.

---

# 7. Why Spring Data JPA?

Spring Data JPA reduces repetitive repository code.

Instead of manually writing database connection and CRUD logic for every operation, repositories can provide operations such as:

```text
save()
findById()
findAll()
delete()
```

It also supports query methods and pagination.

This is particularly useful for the appointment search requirement.

---

# 8. Why REST APIs?

REST APIs provide a clean interface between the frontend and backend.

The UI does not need to know how the database works.

Instead:

```text
Frontend
   ↓ HTTP
REST API
   ↓
Business Logic
   ↓
Database
```

For example:

```http
POST /api/appointments
```

creates an appointment.

The frontend only needs to know:

* Endpoint
* HTTP method
* Request format
* Response format
* Error behaviour

It does not need to directly access PostgreSQL.

---

# 9. Why Separate Controller and Service Layers?

A major design decision was to keep controllers thin.

### Controller responsibility

The controller handles:

* HTTP requests
* Request parameters
* Request/response objects
* HTTP status codes

### Service responsibility

The service handles:

* Business rules
* Validation
* Appointment state changes
* Conflict checks
* Time-based processing

### Repository responsibility

The repository handles:

* Database interaction
* Queries
* Persistence

Therefore:

```text
Controller
   ↓
Service
   ↓
Repository
```

This separation prevents business logic from becoming tightly coupled to HTTP code.

---

# 10. Why DTOs?

DTOs (Data Transfer Objects) are used where appropriate to separate API request/response structures from database entities.

For example, an appointment creation request can contain:

```json
{
  "doctorId": 1,
  "patientId": 1,
  "startTime": "2026-09-20T10:15:00",
  "endTime": "2026-09-20T10:45:00"
}
```

The API does not need to expose the entire internal database entity.

This provides:

* Cleaner API contracts
* Better validation
* Less accidental data exposure
* Easier future API changes

---

# 11. Why Authentication?

An appointment system contains user-specific information.

Therefore, registration and login are required before accessing protected functionality.

The authentication flow is:

```text
Register
   ↓
Credentials stored securely
   ↓
Login
   ↓
Authentication
   ↓
Protected API access
```

Passwords should never be stored as plain text.

Authentication also provides the foundation for future role-based access such as:

```text
PATIENT
DOCTOR
ADMIN
```

---

# 12. Why Search?

A clinic can eventually have a large number of appointments.

Returning every appointment to the UI is inefficient.

Search allows users to find relevant records without manually scanning the complete dataset.

Conceptually:

```text
All Appointments
       ↓
     Search
       ↓
Matching Appointments
```

Possible search criteria can include fields such as patient, doctor, appointment status, or other supported appointment information.

---

# 13. Why Pagination?

Without pagination:

```text
GET /api/appointments
```

could potentially return thousands of records.

That creates unnecessary:

* Database work
* Network traffic
* Memory usage
* Frontend rendering work

Pagination changes this to:

```text
Page 0 → 10 records
Page 1 → 10 records
Page 2 → 10 records
...
```

The API can therefore return only the data required by the current UI page.

---

# 14. Why Sorting?

Users may need to view appointments in different orders.

For example:

```text
Earliest appointment first
Latest appointment first
```

Sorting is therefore supported at the API/database level rather than requiring the frontend to load everything and sort it locally.

Example:

```http
GET /api/appointments?page=0&size=10&sort=startTime,asc
```

This is more scalable because the database performs the ordering before returning the requested page.

---

# 15. Why a Virtual Clock?

The appointment system contains time-dependent behaviour.

One important rule is:

```text
If an appointment remains BOOKED
30 minutes after its start time,
mark it as NO_SHOW.
```

Testing this with the real system clock would require waiting for actual time to pass.

That is inefficient and makes automated testing difficult.

Therefore, a virtual clock was introduced.

```text
POST /clock
       ↓
Set application time
       ↓
Process time-dependent rules
```

Example:

```text
Appointment:
10:15 - 10:45

Virtual clock:
10:45

Current status:
BOOKED

Result:
NO_SHOW
```

This makes the behaviour:

* Deterministic
* Testable
* Fast
* Independent of real-world waiting

---

# 16. Why Trigger Time Processing Through `/clock`?

Instead of continuously running a background timer during development/testing, the system allows the virtual clock to be explicitly advanced.

This gives predictable behaviour:

```text
POST /clock
     ↓
Update virtual time
     ↓
Check appointments
     ↓
Apply time-based transitions
```

This approach is particularly useful in an assessment environment because the evaluator can explicitly control the time and verify the resulting appointment state.

---

# 17. Appointment State Design

Appointment state is represented explicitly rather than inferred from multiple unrelated fields.

The important states include:

```text
BOOKED
   │
   ├──────────────→ COMPLETED
   │
   └──────────────→ NO_SHOW
```

This provides a clear lifecycle.

It also makes queries easier:

```text
Find all BOOKED appointments
Find all COMPLETED appointments
Find all NO_SHOW appointments
```

---

# 18. Why Notifications Are a Separate Service?

Notification logic is separated from the core appointment logic.

Instead of putting notification code directly into every controller:

```text
AppointmentController
     ↓
NotificationService
```

The service can be reused whenever an event occurs.

For example:

```text
Appointment Created
       ↓
NotificationService
       ↓
Confirmation Notification
```

This keeps the appointment workflow focused on appointment business rules.

---

# 19. Why a Web UI?

REST APIs alone are not sufficient for a complete user-facing product.

A usable UI provides a practical way for users to:

* Register
* Login
* Search
* View appointments
* Book appointments
* View appointment status

The UI communicates with the backend through REST APIs.

```text
                 ┌─────────────┐
                 │     UI      │
                 └──────┬──────┘
                        │
                     HTTP
                        │
                 ┌──────▼──────┐
                 │ REST API    │
                 └──────┬──────┘
                        │
                 ┌──────▼──────┐
                 │ PostgreSQL  │
                 └─────────────┘
```

---

# 20. Why a Landing Page?

The landing page communicates the product before the user enters the application.

It explains:

* What the product does
* Who it is for
* Key features
* How it helps users
* Potential future improvements

This separates product presentation from application functionality.

---

# 21. Error Handling Reasoning

The API should distinguish between different types of failures.

For example:

```text
400 → Invalid request
401 → Not authenticated
403 → Not authorized
404 → Resource not found
409 → Conflict
500 → Unexpected server error
```

This is better than returning the same generic error for every failure because clients can respond appropriately.

For example, an appointment conflict can return:

```text
409 CONFLICT
```

instead of:

```text
500 INTERNAL SERVER ERROR
```

---

# 22. Appointment Conflict Validation

Appointment booking requires validation before creating a record.

The system should ensure that invalid or conflicting appointments are not accepted.

Conceptually:

```text
New Appointment
       ↓
Validate doctor
       ↓
Validate patient
       ↓
Validate time range
       ↓
Check existing appointments
       ↓
No conflict?
    /       \
  YES        NO
   ↓          ↓
Create      Reject
```

This prevents overlapping bookings where the business rules do not allow them.

---

# 23. How the Application Was Tested

Testing was performed progressively instead of testing the entire application only at the end.

The general process was:

```text
Implement
   ↓
Compile
   ↓
Start application
   ↓
Call API
   ↓
Check response
   ↓
Check database
   ↓
Fix issue
   ↓
Repeat
```

This made it easier to isolate problems.

---

# 24. API Testing

Core APIs were tested using HTTP requests/cURL.

For example:

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "patientId": 1,
    "startTime": "2026-09-20T10:15:00",
    "endTime": "2026-09-20T10:45:00"
  }'
```

The response was checked for:

* HTTP status
* JSON response
* Created appointment ID
* Appointment status
* Persisted database data

---

# 25. Database Persistence Testing

Persistence was tested by:

```text
Create appointment
       ↓
Verify API response
       ↓
Restart application
       ↓
Request appointment again
       ↓
Verify record still exists
```

This confirms that the application is using real database persistence instead of relying only on application memory.

---

# 26. Virtual Clock Testing

The virtual clock was tested by creating an appointment and then advancing the clock.

Example:

```text
Appointment start:
10:15

Initial status:
BOOKED

Set virtual clock:
10:44

Expected:
BOOKED
```

Then:

```text
Set virtual clock:
10:45

Expected:
NO_SHOW
```

This tests the boundary condition of the 30-minute rule.

---

# 27. Boundary Testing

Time-based systems require special attention to boundary values.

For the no-show rule:

```text
start + 29 minutes
→ BOOKED

start + 30 minutes
→ NO_SHOW
```

The exact boundary is important because an off-by-one-minute implementation could produce incorrect appointment states.

---

# 28. Pagination Testing

Pagination was tested with datasets containing multiple appointments.

For example:

```text
size = 10
```

The API should return at most 10 records per page.

Tests should verify:

```text
Page 0
Page 1
Page 2
```

and ensure that records are not unintentionally skipped or duplicated.

---

# 29. Sorting Testing

Sorting was tested using appointments with different start times.

Example:

```text
09:00
11:00
10:00
```

Ascending sorting should produce:

```text
09:00
10:00
11:00
```

Descending sorting should produce:

```text
11:00
10:00
09:00
```

---

# 30. Problems Encountered and Fixing Approach

During development, issues were handled by first reproducing the problem and then tracing it through the application layers.

The debugging approach was:

```text
Problem
  ↓
Reproduce
  ↓
Check HTTP request
  ↓
Check Controller
  ↓
Check Service
  ↓
Check Repository
  ↓
Check Database
  ↓
Identify root cause
  ↓
Apply fix
  ↓
Retest
```

This prevents fixing only the visible symptom while leaving the underlying problem unresolved.

---

# 31. Compilation and Dependency Issues

When dependency or compilation issues occurred, the first step was to run:

```bash
mvn clean install
```

This helped identify:

* Missing dependencies
* Incorrect imports
* Compilation errors
* Configuration issues
* Test failures

After fixing the underlying issue, the build was run again to verify the fix.

---

# 32. API Debugging

When an API did not behave as expected, the request was tested independently using cURL.

For example:

```text
Frontend
   ↓
   X
API
```

could be isolated as:

```text
cURL
 ↓
API
```

If cURL worked but the UI failed, the problem was likely in the frontend/API integration.

If cURL itself failed, the investigation continued through:

```text
Controller
 → Service
 → Repository
 → Database
```

This helped separate frontend problems from backend problems.

---

# 33. Database Debugging

Database-related issues were investigated by checking:

* Database connection
* Credentials
* Table structure
* Foreign keys
* Entity mappings
* Generated SQL
* Existing records

The goal was to determine whether the problem was:

```text
Connection
or
Mapping
or
Query
or
Data
```

rather than assuming every persistence issue was a database failure.

---

# 34. Configuration Reasoning

Configuration was kept separate from business logic.

Database settings belong in application configuration:

```text
application.properties
```

rather than being hard-coded inside Java classes.

The application therefore follows:

```text
Environment
      ↓
Configuration
      ↓
Spring Boot
      ↓
Application
```

This makes it easier to use different configurations for:

```text
Local Development
Testing
Production
```

without changing application code.

---

# 35. Security Reasoning

Sensitive values such as:

```text
Database passwords
JWT secrets
API keys
Production credentials
```

should not be committed to Git.

The repository should contain configuration templates/examples where necessary, while real secrets remain environment-specific.

This reduces the risk of accidentally exposing credentials through source control.

---

# 36. Why This Architecture?

The final architecture intentionally follows separation of concerns:

```text
┌──────────────────────────┐
│          UI              │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       Controllers        │
│        REST API          │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│        Services          │
│    Business Logic        │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       Repositories       │
│      Data Access         │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       PostgreSQL         │
│     Persistent Data      │
└──────────────────────────┘
```

Each layer has a focused responsibility.

This makes the application easier to:

* Understand
* Debug
* Test
* Extend
* Maintain

---

# 37. Main Engineering Trade-offs

## PostgreSQL vs In-Memory Database

**Chosen:** PostgreSQL

Reason:

* Real persistence
* Relational relationships
* Data integrity
* Better representation of a production-style system

Trade-off:

* Requires database setup

---

## JPA/Hibernate vs Raw SQL

**Chosen:** JPA/Hibernate

Reason:

* Less repetitive CRUD code
* Entity relationships
* Spring integration
* Pagination support

Trade-off:

* Developers must understand ORM behaviour
* Complex queries may sometimes require custom queries

---

## Virtual Clock vs Real System Clock

**Chosen:** Virtual Clock

Reason:

* Deterministic testing
* No waiting
* Easy boundary testing
* Easier assessment/grading

Trade-off:

* Adds additional application state and logic

---

## REST API vs Direct Database Access from UI

**Chosen:** REST API

Reason:

* Security boundary
* Clear separation
* Reusable backend
* Easier future mobile/client integration

Trade-off:

* Additional API layer to maintain

---

# 38. Future Improvements

If the system were developed further, the following improvements would be considered:

### 1. Doctor Availability

Introduce explicit doctor schedules and available time slots.

### 2. Online Payments

Add secure payment processing during appointment booking.

### 3. Analytics

Add dashboards for:

* Appointment volume
* Completion rate
* No-show rate
* Doctor utilization

### 4. Automated Notifications

Integrate email/SMS/push notifications for:

* Booking confirmation
* Appointment reminders
* Cancellation
* No-show status

### 5. Automated CI/CD

Add a CI pipeline to automatically:

```text
Push Code
   ↓
Build
   ↓
Run Tests
   ↓
Check Quality
   ↓
Deploy
```

---

# 39. Final Design Summary

The technology choices were made based on the actual requirements of an appointment management system.

| Requirement    | Decision               | Reason                                  |
| -------------- | ---------------------- | --------------------------------------- |
| Backend        | Java + Spring Boot     | Mature REST/backend ecosystem           |
| Database       | PostgreSQL             | Relational data + persistence           |
| ORM            | JPA/Hibernate          | Entity mapping and database abstraction |
| API            | REST                   | Clean frontend/backend separation       |
| Authentication | Spring Security        | Secure application access               |
| Search         | Database/API level     | Efficient filtering                     |
| Pagination     | Database/API level     | Scalable data retrieval                 |
| Sorting        | Database/API level     | Efficient ordered retrieval             |
| Time handling  | Virtual Clock          | Deterministic testing                   |
| Notifications  | Service layer          | Separation of concerns                  |
| UI             | Web UI                 | Usable product interface                |
| Landing page   | Dedicated product page | Product explanation and onboarding      |

The overall design prioritizes **correctness, persistence, testability, maintainability, and clear separation of responsibilities** rather than implementing only the minimum API functionality.
