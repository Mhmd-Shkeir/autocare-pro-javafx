# AutoCare Pro 🚗

A JavaFX-based vehicle maintenance management system developed using Object-Oriented Programming principles.  
The application helps automotive service centers manage customers, vehicles, maintenance services, payments, and service history through an interactive desktop interface.

The system provides a complete workflow from user authentication to vehicle management, service tracking, payment management, and automatic PDF receipt generation.

---

# 📌 Project Overview

AutoCare Pro is a desktop management application designed for automotive maintenance centers.

The goal of the project is to provide a simple and efficient system for managing:

- Customers and vehicles
- Maintenance services
- Service history
- Payments
- Completed and pending services
- Generated receipts

The project demonstrates the practical application of:

- Object-Oriented Programming concepts
- JavaFX GUI development
- Database integration
- CRUD operations
- Data validation
- Software architecture principles

---

# ✨ Features

## 🔐 User Authentication

- Secure login system
- User validation against the database
- Error handling for invalid credentials

---

## 🚘 Vehicle Management

Users can:

- Add new vehicles
- View registered vehicles
- Manage vehicle information
- Track vehicles currently under maintenance

Vehicle information includes:

- Vehicle ID
- License plate
- Owner information
- Maintenance status

---

## 🔧 Maintenance Service Management

The system allows users to:

- Create new maintenance services
- Assign services to vehicles
- Track service progress
- Update service status

Supported service states:

- Pending
- Completed

Examples:

- Oil change
- Air conditioning service
- General maintenance
- Repair operations

---

## 📋 Service History

The application provides:

- Previous service records
- Completed maintenance history
- Service dates
- Service descriptions
- Total cost calculation

---

## 💳 Payment Management

The system manages:

- Service payments
- Payment records
- Total earnings
- Payment history

Features include:

- Payment tracking
- Price calculation
- Discount handling

---

## 🧾 PDF Receipt Generation

AutoCare Pro automatically generates professional PDF receipts after completing services.

Receipt contains:

- Service ID
- Vehicle information
- Service description
- Start and end dates
- Service status
- Discount applied
- Total price

Example:
AutoCare Pro

Service Receipt

Service ID: 17

Car: M1234

Service Description:

Air conditioning service: $45
Hand rent: $10

Discount applied:
15%

Status:
Completed

Total Price:
$46.75


---

# 🏗️ Project Architecture

The project follows a structured Java application architecture.


AutoCare Pro
│
├── src
│ └── application
│ ├── Login.java
│ ├── Main2.java
│ ├── DatabaseConnection.java
│ ├── CarListView.java
│ ├── addCar.java
│ ├── viewServices.java
│ ├── ServicesHistory.java
│ ├── viewPayments.java
│ └── viewPastServices.java
│
├── database
│ └── autocare_database.sql
│
└── receipts
├── receipt_17.pdf
└── receipt_21.pdf



---

# 🛠️ Technologies Used

## Programming Language

- Java

## User Interface

- JavaFX

## Database

- Microsoft SQL Server

## Database Connectivity

- JDBC

## Build System

- Apache Ant

## Concepts Applied

- Object-Oriented Programming
- Encapsulation
- Inheritance
- Polymorphism
- Classes and Objects
- Interfaces
- Exception Handling
- Data Validation

---

# 🗄️ Database

The project uses Microsoft SQL Server.

The database contains tables for managing:

- Users
- Vehicles
- Services
- Payments
- Maintenance records

## Database script:
### database/autocare_database.sql



---

# ⚙️ Installation & Setup

## Requirements

Install:

- Java JDK 17+
- JavaFX SDK
- Microsoft SQL Server
- SQL Server JDBC Driver
- IDE (Eclipse / IntelliJ / NetBeans)

---

## Database Setup

1. Open SQL Server Management Studio

2. Execute:
database/autocare_database.sql


3. Update database connection settings:

File:


src/application/DatabaseConnection.java


Configure:

```java
Server
Database name
Username
Password


## ⚙️ Installation & Setup

### Requirements

Install the following:

- Java JDK 17+
- JavaFX SDK
- Microsoft SQL Server
- SQL Server JDBC Driver
- Java IDE (Eclipse / IntelliJ IDEA / NetBeans)

---

## 🗄️ Database Setup

1. Open SQL Server Management Studio.

2. Execute the database script:

```sql
database/autocare_database.sql
```

3. Configure the database connection in:

```
src/application/DatabaseConnection.java
```

Update the following values according to your SQL Server configuration:

- Server name
- Database name
- Username
- Password

---

# 🚀 Running the Application

1. Clone the repository:

```bash
git clone https://github.com/Mhmd-Shkeir/autocare-pro-javafx.git
```

2. Open the project using your preferred Java IDE.

3. Configure the JavaFX libraries and dependencies.

4. Make sure the database server is running.

5. Run the main application:

```
Main2.java
```

---

# 📸 Screenshots

(Add application screenshots here)

Recommended screenshots:

- Login page
- Vehicle management interface
- Add vehicle screen
- Service management screen
- Service history page
- Payment management screen
- Generated PDF receipt

---

# 🎯 Learning Objectives Achieved

This project demonstrates:

✅ JavaFX desktop application development  
✅ Database-driven application design  
✅ CRUD operations implementation  
✅ Object-Oriented Programming principles  
✅ GUI development using JavaFX  
✅ JDBC database integration  
✅ PDF receipt generation  
✅ Data validation and error handling  
✅ Software design and project organization  

---

# 🚀 Future Improvements

Possible improvements:

- Online appointment booking system
- Customer notification system
- Cloud database integration
- Enhanced authentication and authorization
- Analytics and reporting dashboard
- Employee management module
- Automated maintenance reminders
- Mobile application integration

---

# 👨‍💻 Authors

Developed by:

- Mohammad Ali Shkeir
- Mostafa
- Mohammad Karnib

---

# 📄 License

This project was developed for educational purposes as part of the **Object-Oriented Programming II** course project.
