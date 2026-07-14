-- Create Users table
CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

-- Create Cars table
CREATE TABLE Cars (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    owner_name NVARCHAR(100) NOT NULL,
    owner_contact VARCHAR(20) NOT NULL,
    car_model NVARCHAR(100) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create Maintenance table
CREATE TABLE Maintenance (
    maintenance_id INT IDENTITY PRIMARY KEY,
    car_id INT NOT NULL,
    user_id INT NOT NULL,
    description NVARCHAR(255),
    maintenance_start DATETIME DEFAULT GETDATE(),
    total_price DECIMAL(10, 2),
    discounted_price DECIMAL(10, 2),
    FOREIGN KEY (car_id) REFERENCES Cars(id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create Completed_Maintenance table (without foreign key constraint to maintenance)
CREATE TABLE Completed_Maintenance (
    maintenance_id INT PRIMARY KEY,  -- Just a regular primary key, not a foreign key
    car_id INT NOT NULL,
    user_id INT NOT NULL,
    maintenance_start DATETIME,
    maintenance_end DATETIME DEFAULT GETDATE(),
    description NVARCHAR(255),
    payment_amount FLOAT,
    payment_status NVARCHAR(50),
    paid_amount FLOAT,
    discounted_price FLOAT,
    total_price DECIMAL(10, 2),
    FOREIGN KEY (car_id) REFERENCES Cars(id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create Installment_Payments table
CREATE TABLE Installment_Payments (
    payment_id INT PRIMARY KEY IDENTITY(1,1),
    maintenance_id INT,
    amount FLOAT NOT NULL,
    payment_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (maintenance_id) REFERENCES Completed_Maintenance(maintenance_id)
);