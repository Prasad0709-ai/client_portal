# Client Portal & Project Management System (JV-CRM-005)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.5.3-blue.svg)](https://hibernate.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.x-38bdf8.svg)](https://tailwindcss.com/)
[![Security](https://img.shields.io/badge/Spring%20Security-6.x-green.svg)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

An enterprise-grade, full-stack **Client Portal & Project Management System** built with **Spring Boot 3.3.4**, **Hibernate 6.x**, **Spring Data JPA**, **MySQL 8.x** (with resilient embedded H2 auto-fallback), **Spring Security 6**, **Thymeleaf**, **Tailwind CSS**, and **Chart.js**.

---

## Table of Contents
1. [Core Features](#core-features)
2. [Tech Stack & Architecture](#tech-stack--architecture)
3. [Pre-Seeded Demo Accounts](#pre-seeded-demo-accounts)
4. [Enterprise Pipeline Benchmark](#enterprise-pipeline-benchmark)
5. [Prerequisites & Environment Setup](#prerequisites--environment-setup)
6. [How to Run Locally](#how-to-run-locally)
7. [API Endpoints](#api-endpoints)
8. [GitHub Repository Setup](#github-repository-setup)
9. [Cloud Deployment Guide (Live URL)](#cloud-deployment-guide-live-url)
10. [Automated Test Suite](#automated-test-suite)

---

## Core Features

- **Role-Based Access Control (RBAC)**: Strict URL-level and method-level access control (`ROLE_ADMIN` and `ROLE_CLIENT`) using Spring Security 6, BCrypt encryption (strength 12), and CSRF token protection.
- **Smart Post-Login Role Routing**: Automatically directs admins to `/admin/dashboard` and clients to `/clients/dashboard`.
- **Project Tracking & Lifecycle Management**: Create, update, view, and delete projects across stages (`PLANNING`, `IN_PROGRESS`, `ON_HOLD`, `COMPLETED`, `CANCELLED`).
- **Milestones & Task Management**: Real-time progress math calculation (`completedMilestones / totalMilestones * 100`) and 1-click status toggles.
- **Two-Way Document Sharing**: Secure disk-backed file uploads and streaming downloads for both Admins and Clients with tenant isolation.
- **Project Communication & Messaging Channel**: Real-time project discussion feed allowing clients to post inquiries and admins to post updates with author badges and timestamps.
- **Predictive Analytics & ML Forecasting**: Milestone velocity modeling, deadline slippage variance, multi-factor risk index (0–100), and automated AI remediation recommendations.
- **Interactive Visualizations (Chart.js)**: Financial capital pipeline attainment bar charts and project portfolio distribution charts.
- **Dual-Engine Resilient Data Layer**: Dynamically probes MySQL 8.x on port 3306 and seamlessly falls back to embedded H2 in MySQL-compatibility mode if MySQL is offline.

---

## Tech Stack & Architecture

- **Backend**: Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, Spring Security 6
- **ORM / Database**: Hibernate 6.5.3, MySQL 8.x (production), H2 Database (resilient zero-config fallback)
- **Frontend & Templates**: Thymeleaf 3.1, Tailwind CSS (via high-speed CDN), Google Fonts Inter
- **Data Visualizations**: Chart.js 4.x
- **Build & Dependency Management**: Apache Maven 3.9
- **Containerization**: Docker multi-stage build (Alpine Linux + Eclipse Temurin 21)

---

## Pre-Seeded Demo Accounts

The system automatically initializes test accounts upon first startup:

| Account Type | Email | Password | Company / Tenant | Default Route |
|---|---|---|---|---|
| **Executive Admin** | `admin@portal.com` | `admin123` | Data Alcott Systems | `/admin/dashboard` |
| **Enterprise Client 1** | `client@techretail.com` | `client123` | TechRetail Inc. | `/clients/dashboard` |
| **Enterprise Client 2** | `client2@novabanking.com` | `client123` | Nova Banking Solutions | `/clients/dashboard` |

---

## Enterprise Pipeline Benchmark

The system monitors an administrative pipeline tracking baseline of **$45,892.00**:
- Invoice 1: `$14,500.00` (`PAID`)
- Invoice 2: `$16,800.00` (`UNPAID`)
- Invoice 3: `$14,592.00` (`UNPAID`)
- **Total Aggregated Pipeline**: **`$45,892.00`** (100% Attainment against baseline metric).

---

## Prerequisites & Environment Setup

- **Java JDK**: Java 21 (or 17+)
- **Build Tool**: Apache Maven 3.9+
- **Database (Optional)**: MySQL 8.x on port 3306 (database `clientportal_db`). If MySQL is not running, the application automatically uses the embedded in-memory database.

---

## How to Run Locally

### Option 1: 1-Click Batch Launcher (Windows CMD)
Double-click `start.bat` or run:
```cmd
start.bat
```

### Option 2: Using Maven
```bash
mvn spring-boot:run
```

### Option 3: Using Pre-Built Executable JAR
```bash
mvn clean package -DskipTests
java -jar target/client-portal-1.0.0.jar
```

### Open the Application
Navigate your browser to:
👉 **`http://localhost:8080/login`**

- Click **Admin Role** (`admin@portal.com` / `admin123`) to explore the executive dashboard.
- Click **Client Role** (`client@techretail.com` / `client123`) to view the tenant-isolated client portal.

---

## API Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/login` | Authentication view | Public |
| `GET` | `/admin/dashboard` | Executive management dashboard | `ROLE_ADMIN` |
| `GET` | `/admin/projects/{id}` | Deep project workspace, discussion & invoices | `ROLE_ADMIN` |
| `POST` | `/admin/projects/{id}/messages` | Post update to project discussion | `ROLE_ADMIN` |
| `GET` | `/clients/dashboard` | Client workspace, deliverables & billing | `ROLE_CLIENT` |
| `POST` | `/clients/projects/{id}/messages` | Post client inquiry to project discussion | `ROLE_CLIENT` |
| `POST` | `/clients/projects/{id}/documents/upload` | Upload document to assigned project | `ROLE_CLIENT` |
| `GET` | `/documents/download/{id}` | Secure streaming document download | Authenticated |
| `GET` | `/api/analytics/dashboard` | KPI metrics, pipeline revenue & ML forecasts | Authenticated |
| `GET` | `/api/analytics/chart-data` | Status and financial distributions for Chart.js | Authenticated |

---

## GitHub Repository Setup

To push this project to GitHub under account **`Prasad0709-ai`**:

```bash
# 1. Initialize git repository
git init

# 2. Stage all files
git add .

# 3. Commit changes
git commit -m "Initial commit: Client Portal & Project Management enterprise system"

# 4. Set default branch to main
git branch -M main

# 5. Add remote repository
git remote add origin https://github.com/Prasad0709-ai/Client-Portal-Project-Management.git

# 6. Push to GitHub (enter your GitHub credentials / token when prompted)
git push -u origin main
```

---

## Cloud Deployment Guide (Live URL)

### Deploying to Render (Free Web Service)
1. Fork or push the repository to GitHub: `https://github.com/Prasad0709-ai/Client-Portal-Project-Management`.
2. Log in to [Render.com](https://render.com) and click **New +** $\rightarrow$ **Web Service**.
3. Connect your GitHub repository `Client-Portal-Project-Management`.
4. Render will auto-detect the `Dockerfile` and `render.yaml`.
5. Select the **Free** instance type.
6. Click **Create Web Service**.
7. In ~3 minutes, Render provides your live public URL: `https://client-portal-management.onrender.com`.

### Deploying to Railway
1. Go to [Railway.app](https://railway.app) and select **New Project** $\rightarrow$ **Deploy from GitHub Repo**.
2. Select `Client-Portal-Project-Management`.
3. Railway automatically builds the Docker container and assigns a public HTTPS domain.

---

## Automated Test Suite

Run the complete 17 automated integration, RBAC, and ML forecast tests:
```bash
mvn test
```
