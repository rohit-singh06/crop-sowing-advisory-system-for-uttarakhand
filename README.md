# Crop Sowing Advisor System

A smart desktop application to empower farmers and agricultural experts with crop advice, market prices, and access to government agricultural services. Built with Java Swing and MySQL.

## Features

- **Farmer Registration:** Farmers can register using their Aadhaar number and get a unique Farmer ID.
- **Expert Registration:** Agricultural experts can register and provide crop advice for different districts and crops.
- **Personalized Crop Advice:** Farmers receive tailored advice for selected crops and districts.
- **Market Price Checker:** View simulated real-time market prices for major crops.
- **Nearest Government Centers:** Find the 5 nearest agricultural government centers (KVKs, Soil Labs, etc.) based on your location.
- **Modern UI:** Clean, user-friendly interface with icons and responsive design.

## Getting Started

### Prerequisites

- **Java JDK 8 or higher**
- **MySQL Server**
- **MySQL Connector/J** (already included in `lib/`)

### Database Setup

1. **Create the database:**

   ```sql
   CREATE DATABASE cropadvisor;
   USE cropadvisor;
   ```

2. **Create the required tables:**

   ```sql
   CREATE TABLE farmers (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       aadhaar_number VARCHAR(12) NOT NULL UNIQUE,
       district VARCHAR(50) NOT NULL,
       village VARCHAR(100) NOT NULL,
       regdate DATETIME NOT NULL
   );

   CREATE TABLE experts (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       registration_number VARCHAR(50) NOT NULL
   );

   CREATE TABLE advice (
       id INT AUTO_INCREMENT PRIMARY KEY,
       district VARCHAR(50) NOT NULL,
       crop VARCHAR(50) NOT NULL,
       advice TEXT NOT NULL,
       expert_id INT,
       FOREIGN KEY (expert_id) REFERENCES experts(id)
   );
   ```

3. **Update your MySQL credentials in `src/DatabaseConnection.java` if needed:**

   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/cropadvisor";
   private static final String USER = "root";
   private static final String PASSWORD = "";
   ```

### Build & Run

1. **Compile the Java source files:**

   ```sh
   javac -cp "lib/mysql-connector-j-9.2.0.jar;src" -d bin src/*.java
   ```

2. **Run the application:**

   ```sh
   java -cp "bin;lib/mysql-connector-j-9.2.0.jar" MainApplication
   ```

   > On Linux/Mac, replace `;` with `:` in the classpath.

### Directory Structure

```
crop sowing advisor system/
├── bin/                # Compiled .class files
├── lib/                # MySQL Connector/J
├── src/                # Java source files
│   ├── icons/          # UI icons
│   └── MainApplication.java
└── README.md           # (This file)
```

## Customization

- **Add more crops or districts:** Edit the `crops` and `districts` arrays in `MainApplication.java`.
- **UI Icons:** Place your PNG icons in `src/icons/`.
- **Market Prices:** Simulated in code; connect to a real API for live prices.

## Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.
