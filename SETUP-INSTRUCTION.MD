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

The project includes a `docker-compose.yml` file to easily start a LocalStack container with the necessary AWS services. The built-in healthcheck will ensure the command doesn't exit until the services are fully ready.

In your terminal, from the root of the project directory, run:

```sh
docker-compose up -d
```

### Step 3: Run the Spring Boot Application

To connect to LocalStack, you must activate the `local` Spring profile when running the application.

#### Option A: From the Terminal (Recommended)

In your terminal, from the root of the project directory, run:

```sh
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### Option B: From an IDE (e.g., IntelliJ IDEA)

1.  Open the project in your IDE.
2.  Go to **Run -> Edit Configurations...**
3.  Find the run configuration for `BulkProductsUploadApplication`.
4.  Add the following to the **"Environment variables"** field:
    -   **Name**: `SPRING_PROFILES_ACTIVE`
    -   **Value**: `local`
5.  Save the configuration and click the "Run" button.

The application will start on `http://localhost:8080`.

---

## Verification

Once the application is running, you can verify that everything is working correctly:

1.  **Check the startup logs**: You should see the line `The following 1 profile is active: "local"`.
2.  **Open Swagger UI**: Navigate to [**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html).
3.  **Use the API endpoints** to upload and retrieve products. The data will be saved in the running LocalStack container.

---

## How It Works

-   **Spring Profiles**: The project uses a Spring Profile named `local` to separate local and production configurations.
-   **Activation**: The `local` profile is activated by passing a command-line argument or environment variable.
-   **`AwsLocalConfig.java`**: When the `local` profile is active, this class creates AWS client beans that point to the LocalStack endpoint (`http://localhost:4566`) and use fake credentials.
-   **`AwsProdConfig.java`**: In a production environment (when the `local` profile is not active), this class creates standard AWS client beans that use the default credential provider chain.
