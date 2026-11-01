# ThinkMart

ThinkMart is a Spring Boot e-commerce demo application built with Java 17, Spring MVC, Spring Data JPA, Thymeleaf, and MySQL.

## Features

- Product catalog page with search, category filters, and sorting
- Session-based shopping cart with quantity controls
- Checkout flow with validation, order persistence, and stock reduction
- About and Contact pages with validation and database persistence
- Sample product data for first-run setup
- Responsive storefront UI using Thymeleaf templates and CSS

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8+
- VS Code or another Java IDE

## MySQL setup

1. Log in to MySQL and create the database:

   ```sql
   CREATE DATABASE thinkmart_db;
   CREATE USER 'thinkmart_user'@'localhost' IDENTIFIED BY 'your_secure_password';
   GRANT ALL PRIVILEGES ON thinkmart_db.* TO 'thinkmart_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. Copy `.env.example` to `.env` and update the values.

3. Start the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

## Environment variables

The application reads database connection details from environment variables. For local development, create a `.env` file with:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/thinkmart_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=thinkmart_user
DB_PASSWORD=your_secure_password
```

Do not commit `.env` files with real credentials.

## Run in VS Code

From the project root:

```bash
mvn spring-boot:run
```

Application URL:

- Homepage: http://localhost:8080/products
- Cart: http://localhost:8080/cart
- Checkout: http://localhost:8080/checkout
- About: http://localhost:8080/about
- Contact: http://localhost:8080/contact

## Replace sample product images

The app uses remote Unsplash image URLs by default. Replace them in the database seed data or update `DataInitializer` if you want custom product photography.

## Testing

```bash
mvn test
```

## Notes

This is a demonstration storefront and is not a production-ready payment system.
