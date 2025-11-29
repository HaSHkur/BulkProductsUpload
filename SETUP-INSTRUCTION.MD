# Reviewer's Local Setup Guide

This guide provides step-by-step instructions to set up and run the project locally for review. The entire process uses **LocalStack** to simulate AWS services (S3 and DynamoDB) on your machine, so **no real AWS account or credentials are required**.

---

## Prerequisites

Please ensure you have the following software installed on your system:

1.  **Java (JDK 21)**: The project is built using Java 21.
2.  **Docker & Docker Compose**: Required to run the local AWS environment.
3.  **Git**: For cloning the repository.
4.  **An IDE** (Optional but Recommended): IntelliJ IDEA or VSCode with Java extensions.

---

## Setup in 3 Simple Steps

### Step 1: Clone the Repository

Open your terminal and clone the project to your local machine.

```sh
git clone <repository-url>
cd BulkProductsUpload
```

### Step 2: Start the Local AWS Environment

The project includes a `docker-compose.yml` file to easily start a LocalStack container with the necessary AWS services.

In your terminal, from the root of the project directory, run:

```sh
docker-compose up -d
```

This command will:
- Download the LocalStack Docker image.
- Start a container in the background (`-d`).
- Configure it to run S3 and DynamoDB services on `http://localhost:4566`.

### Step 3: Run the Spring Boot Application

The project is pre-configured to automatically detect and connect to the LocalStack container.

1.  **Open the project** in your favorite IDE (e.g., IntelliJ IDEA).
2.  Locate the `BulkProductsUploadApplication.java` file.
3.  **Run the `main` method**.

Alternatively, you can run the application from the terminal:

```sh
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

---

## Verification

Once the application is running, you can verify that everything is working correctly:

1.  **Open your web browser** and navigate to the Swagger UI documentation:
    [**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

2.  **Use the API endpoints** to upload products and images. The data will be saved in the running LocalStack container, not in a real AWS account.

---

## How It Works

-   **Spring Profiles**: The project uses a Spring Profile named `local`.
-   **`application-local.properties`**: This file exists in `src/main/resources` and automatically activates the `local` profile.
-   **`AwsLocalConfig.java`**: When the `local` profile is active, this configuration class creates AWS client beans that point to the LocalStack endpoint (`http://localhost:4566`).
-   **`AwsProdConfig.java`**: In a production environment (where the `local` profile is not active), this class creates standard AWS client beans.

This setup ensures a clean separation between local development and production environments.
