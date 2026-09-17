# Auriga Clinic Appointment Booking

A full-stack **Clinic Appointment Booking System** designed to manage doctors, patients, appointments, authentication, appointment status transitions, notifications, and time-based appointment workflows.

The application provides a REST API backend, persistent database storage, authentication, search, pagination and sorting, along with a usable web UI for interacting with the system.

---

## Overview

The system allows patients and clinic staff to manage appointments through a centralized platform.

### Target Audience

* Patients
* Doctors
* Clinic administrators
* Reception / support staff

### What the system helps with

* User registration and login
* Doctor and patient management
* Appointment booking
* Appointment search
* Appointment status management
* Persistent storage using a relational database
* Pagination and sorting
* Time-based appointment processing
* Automatic no-show handling
* Notifications
* REST API access
* Web-based user interface

---

# Key Features

## 1. User Registration & Login

Users can create an account and authenticate before accessing protected application functionality.

Typical authentication flow:

```text
Register
   ↓
Login
   ↓
Authenticated User
   ↓
Access Application Features
```

---

## 2. Appointment Booking

Users can create appointments by providing:

* Doctor
* Patient
* Start time
* End time

Example:

```http
POST /api/appointments
Content-Type: application/json
```

```json
{
  "doctorId": 1,
  "patientId": 1,
  "startTime": "2026-09-20T10:15:00",
  "endTime": "2026-09-20T10:45:00"
}
```

---

## 3. Real Database Persistence

The application uses a relational database instead of in-memory storage.

The database stores the application's core entities and relationships.

### Core schema

```text
USER
 ├── id
 ├── name
 ├── email
 ├── password
 └── role

DOCTOR
 ├── id
 ├── name
 └── ...

PATIENT
 ├── id
 ├── name
 └── ...

APPOINTMENT
 ├── id
 ├── doctor_id
 ├── patient_id
 ├── start_time
 ├── end_time
 └── status

NOTIFICATION
 ├── id
 ├── user_id
 ├── appointment_id
 ├── message
 └── created_at
```

### Appointment Status

Appointments can move through states such as:

```text
BOOKED
   │
   ├── COMPLETED
   │
   └── NO_SHOW
```

The application uses a virtual clock to support deterministic time-based processing.

---

# Virtual Clock

The project includes a virtual clock mechanism that allows appointment-related time events to be tested without waiting for real-world time to pass.

The clock can be updated through:

```http
POST /clock
```

Example:

```json
{
  "currentTime": "2026-09-20T11:15:00"
}
```

### Automatic No-Show

If the virtual clock moves **30 minutes beyond an appointment's start time**, and the appointment is still `BOOKED`, the system can automatically transition it to:

```text
NO_SHOW
```

Example:

```text
Appointment Start
10:15
   ↓
Clock reaches
10:45
   ↓
Appointment still BOOKED
   ↓
Automatically becomes
NO_SHOW
```

This makes time-dependent appointment logic easy to test and grade deterministically.

---

# REST API

The backend exposes REST APIs for the core application operations.

## Authentication

| Method | Endpoint             | Description                   |
| ------ | -------------------- | ----------------------------- |
| `POST` | `/api/auth/register` | Register a new user           |
| `POST` | `/api/auth/login`    | Authenticate an existing user |

---

## Appointments

| Method      | Endpoint                 | Description                     |
| ----------- | ------------------------ | ------------------------------- |
| `POST`      | `/api/appointments`      | Create/book an appointment      |
| `GET`       | `/api/appointments`      | Retrieve appointments           |
| `GET`       | `/api/appointments/{id}` | Retrieve a specific appointment |
| `PUT/PATCH` | `/api/appointments/{id}` | Update appointment information  |
| `DELETE`    | `/api/appointments/{id}` | Cancel/delete an appointment    |

### Create Appointment

```http
POST /api/appointments
```

Request:

```json
{
  "doctorId": 1,
  "patientId": 1,
  "startTime": "2026-09-20T10:15:00",
  "endTime": "2026-09-20T10:45:00"
}
```

---

## Search

Appointments can be retrieved using search/filter parameters.

Example:

```http
GET /api/appointments?search=John
```

Additional filters can be provided according to the supported appointment fields.

---

## Pagination & Sorting

Collection APIs support pagination and sorting to avoid returning the entire dataset at once.

Example:

```http
GET /api/appointments?page=0&size=10
```

Sorting example:

```http
GET /api/appointments?page=0&size=10&sort=startTime,asc
```

Conceptually:

```text
Database
   ↓
Filter/Search
   ↓
Sort
   ↓
Pagination
   ↓
REST Response
```

This approach allows the application to remain usable as the number of appointments grows.

---

## Virtual Clock

| Method | Endpoint | Description                                |
| ------ | -------- | ------------------------------------------ |
| `POST` | `/clock` | Set/update the application's virtual clock |

Example:

```http
POST /clock
Content-Type: application/json
```

```json
{
  "currentTime": "2026-09-20T11:15:00"
}
```

The clock can trigger time-dependent appointment processing such as automatic `NO_SHOW` transitions.

---

## Notifications

The notification service is responsible for appointment-related notifications.

Typical use cases include:

* Appointment confirmation
* Appointment updates
* Appointment status changes
* No-show notifications

---

# Tech Stack

## Backend

* **Java**
* **Spring Boot**
* Spring Web / REST
* Spring Data JPA
* Hibernate
* Spring Security
* Maven

## Database

* **PostgreSQL**
* JPA / Hibernate ORM

## Frontend

* Web-based UI
* HTML
* CSS
* JavaScript
* REST API integration

## Development

* Visual Studio Code
* Git
* GitHub Codespaces

---

# Project Structure

```text
Auriga_Project/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── auriga/
│   │   │           └── clinic/
│   │   │
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repository/
│   │   │               ├── entity/
│   │   │               ├── dto/
│   │   │               └── ...
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

---

# Prerequisites

Before running the application, install:

* **Java 17+** (use the version configured in `pom.xml`)
* **Maven**
* **PostgreSQL**
* Git

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

Verify PostgreSQL:

```bash
psql --version
```

---

# Database Configuration

Create a PostgreSQL database.

Example:

```sql
CREATE DATABASE auriga_clinic;
```

Create a database user if required:

```sql
CREATE USER auriga_user WITH PASSWORD 'your_password';
```

Grant access:

```sql
GRANT ALL PRIVILEGES ON DATABASE auriga_clinic TO auriga_user;
```

> Use your own secure password. Never commit production credentials to Git.

---

# Application Configuration

Configure the database connection in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auriga_clinic
spring.datasource.username=auriga_user
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

If your project uses environment variables, the same values can be provided through the environment instead of storing credentials directly in `application.properties`.

Example:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Then configure:

```bash
DB_URL=jdbc:postgresql://localhost:5432/auriga_clinic
DB_USERNAME=auriga_user
DB_PASSWORD=your_password
```

### Important

Do not commit:

```text
passwords
API keys
JWT secrets
production database credentials
```

Add sensitive configuration to `.gitignore` where appropriate.

---

# Running the Application

## 1. Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd Auriga_Project
```

## 2. Configure PostgreSQL

Create the database:

```sql
CREATE DATABASE auriga_clinic;
```

Update the database credentials in:

```text
src/main/resources/application.properties
```

## 3. Build the project

```bash
mvn clean install
```

## 4. Start the application

```bash
mvn spring-boot:run
```

The backend will normally start at:

```text
http://localhost:8080
```

---

# Running in GitHub Codespaces

The project can also be developed inside GitHub Codespaces.

After opening the repository:

```bash
mvn clean install
```

Then:

```bash
mvn spring-boot:run
```

Expose port:

```text
8080
```

through the Codespaces **Ports** panel.

The generated forwarded URL can then be used to access the application.

---

# Application Flow

```text
             ┌─────────────────┐
             │     Landing     │
             │      Page       │
             └────────┬────────┘
                      │
              Register / Login
                      │
                      ▼
             ┌─────────────────┐
             │   Application   │
             │      UI         │
             └────────┬────────┘
                      │
             ┌────────▼────────┐
             │   REST APIs     │
             └────────┬────────┘
                      │
             ┌────────▼────────┐
             │ Spring Services │
             └────────┬────────┘
                      │
             ┌────────▼────────┐
             │   PostgreSQL    │
             └─────────────────┘
```

---

# Landing Page

The product includes a one-page landing experience describing:

### What is it?

A clinic appointment management platform that helps patients and clinic staff manage appointments through a centralized system.

### Key Features

* Secure registration and login
* Appointment booking
* Doctor/patient management
* Appointment search
* Pagination and sorting
* Appointment status tracking
* Automatic no-show processing
* Notifications
* Persistent database storage

### Target Audience

```text
Patients
   +
Doctors
   +
Clinic Staff
   +
Administrators
```

### How it Helps

The system centralizes appointment information, reduces manual appointment tracking, provides searchable appointment records, and automates time-dependent appointment status processing.

---

# Future Features

Three potential next features are:

### 1. Online Payments

Allow patients to securely pay consultation fees during appointment booking.

### 2. Doctor Availability & Scheduling

Allow doctors to define:

* Working hours
* Breaks
* Leave dates
* Available appointment slots

The booking system could then prevent invalid bookings automatically.

### 3. Analytics Dashboard

Provide clinic administrators with:

* Daily/monthly appointments
* Completed appointments
* No-show rate
* Doctor-wise appointment statistics
* Patient activity

---

# Example API Request

### Book Appointment

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

### Set Virtual Clock

```bash
curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{
    "currentTime": "2026-09-20T10:45:00"
  }'
```

If the appointment started at `10:15` and remains `BOOKED` when the clock reaches `10:45`, the appointment can be automatically marked:

```text
BOOKED → NO_SHOW
```

---

# Validation & Error Handling

The API should validate:

* Required fields
* Valid doctor/patient IDs
* Valid appointment time ranges
* Duplicate/conflicting appointments
* Authentication requirements
* Invalid requests

The API returns appropriate HTTP status codes such as:

```text
200 OK
201 CREATED
400 BAD REQUEST
401 UNAUTHORIZED
403 FORBIDDEN
404 NOT FOUND
409 CONFLICT
500 INTERNAL SERVER ERROR
```

---

# Security

Security considerations include:

* Passwords should never be stored as plain text.
* Authentication should be required for protected operations.
* Sensitive configuration should not be committed.
* Database credentials should be provided through environment-specific configuration.
* Input validation should be performed at the API boundary.

---

# Testing

Run the test suite using:

```bash
mvn test
```

For manual API testing, tools such as:

* Postman
* cURL
* Browser developer tools

can be used.

---

# Development Checklist

The project covers the following requirements:

* [x] Real database persistence
* [x] Relational database schema
* [x] REST APIs
* [x] User registration
* [x] User login
* [x] Appointment booking
* [x] Search
* [x] Pagination
* [x] Sorting
* [x] Web UI
* [x] Product landing page
* [x] Virtual clock
* [x] Automatic no-show workflow
* [x] Notification service

---

# License

This project is developed for the Auriga IT technical assessment / project evaluation.

---

## Author

**Pranjal Jain**

B.Tech Computer Science & Engineering
Swami Keshvanand Institute of Technology, Management & Gramothan (SKIT), Jaipur
