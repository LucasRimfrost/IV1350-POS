# Point-of-Sale System

## Project Description
This Point-of-Sale (POS) system is a Java application developed with Maven for the Object-Oriented Design (IV1350) course. The system handles sales processing, inventory management, and receipt generation following object-oriented design principles.

## Features
- Process sales with multiple items
- Calculate prices including VAT
- Apply customer discounts
- Process payments and calculate change
- Generate receipts
- Update inventory

## System Architecture
The application follows a layered architecture with Model-View-Controller (MVC) pattern:

- **View Layer**: User interface that displays information and captures user input
- **Controller Layer**: Coordinates operations between view and model
- **Model Layer**: Contains core business logic and domain objects
- **Integration Layer**: Handles external system communication (accounting, inventory)
- **Utility Layer**: Provides supporting functionality like Amount class

## Building and Running

### Prerequisites
- JDK 17 or higher
- Maven 3.6 or higher

### Commands
To compile the project:
```bash
mvn compile
```
To run unit tests:
```bash
mvn test
```
To run the application:
```bash
java -cp target/classes se.kth.iv1350.pos.startup.Main
```

To clean compiled files:
```bash
mvn clean
```

## Getting Started

### Cloning the Project for Seminar 4
To clone this project and access the seminar 4 implementation:

```bash
# Clone the repository with the seminar4 branch directly
git clone -b seminar4 https://github.com/YourUsername/IV1350-POS.git
cd IV1350-POS
