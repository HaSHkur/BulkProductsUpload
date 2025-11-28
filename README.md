# Bulk Products Upload Service

This is a Spring Boot application designed to handle the bulk upload of product data. It provides REST endpoints to accept file uploads, processes them, and integrates with AWS services for persistent storage. The application is containerized using Docker for easy, portable, and consistent deployment across any environment.

## 1. Architecture

The application follows a simple, robust, and scalable architecture:

1.  **Containerization**: The entire application is packaged as a Docker image using a multi-stage `Dockerfile`.
    *   **Build Stage**: Uses a full Gradle and JDK image to compile the source code and build an executable `.jar` file.
    *   **Runtime Stage**: The final image uses a minimal JRE base (`eclipse-temurin:21-jre-alpine`) and copies only the built `.jar` file. This results in a small, secure, and optimized image for production.
2.  **API Layer**: A RESTful API built with Spring Web accepts multipart file uploads.
3.  **Service Layer**: The core business logic resides here, responsible for validating and processing the uploaded files.
4.  **AWS Integration**: The service interacts with AWS for storage:
    *   **Amazon S3**: Used to store the uploaded files (e.g., product images, CSV/JSON data files).
    *   **Amazon DynamoDB**: Used to store structured product metadata, leveraging the DynamoDB Enhanced Client for easy data mapping.
5.  **Configuration**: Application configuration (like AWS region and bucket names) is managed externally via environment variables, following the 12-Factor App methodology.

  <!-- Placeholder for a real diagram -->

---

## 2. Technologies Used

*   **Backend**: Java 21, Spring Boot 3.5.8
*   **Build Tool**: Gradle 8.8.0
*   **API Framework**: Spring Web
*   **Data Validation**: Spring Boot Starter Validation
*   **Cloud Services**:
    *   AWS SDK for Java 2.x
    *   Amazon S3 (for object storage)
    *   Amazon DynamoDB (for NoSQL data persistence)
*   **API Documentation**: SpringDoc OpenAPI (provides an interactive Swagger UI)
*   **Containerization**: Docker
*   **Utilities**: Lombok

---

## 3. Features

*   **RESTful API**: Exposes endpoints for uploading files in bulk.
*   **Cloud Native**: Designed to run in the cloud, with seamless integration for AWS S3 and DynamoDB.
*   **Portable & Consistent**: Containerized with Docker to ensure it runs the same way everywhere.
*   **Configurable**: Key settings like AWS region, S3 bucket, and file size limits can be configured at runtime.
*   **Self-Documenting API**: Automatically generates interactive API documentation with Swagger UI, available at `/swagger-ui.html`.
*   **Health Checks**: Includes Spring Boot Actuator for monitoring application health and metrics at `/actuator/health`.

---

## 4. Getting Started

Follow these instructions to build and run the application locally.

### Prerequisites

*   Docker must be installed and running on your machine.
*   AWS Credentials configured in your environment. The application uses the default AWS SDK credential chain. The easiest way to configure this locally is by setting environment variables:
    *   `AWS_ACCESS_KEY_ID`
    *   `AWS_SECRET_ACCESS_KEY`
    *   `AWS_REGION` (can also be set via the `docker run` command)

### Step 1: Build the Docker Image

Navigate to the project's root directory in your terminal and run the following command. This will execute the `Dockerfile` to build a portable image named `bulk-products-upload`.

```sh
docker build -t bulk-products-upload .
```

### Step 2: Run the Application Container

Once the image is built, run it as a container. You must provide the AWS S3 bucket name as an environment variable.

```sh
docker run -d -p 8080:8080 \
  -e AWS_REGION="ap-southeast-2" \
  -e AWS_S3_BUCKET_NAME="your-actual-s3-bucket-name" \
  -e AWS_ACCESS_KEY_ID="your-aws-access-key" \
  -e AWS_SECRET_ACCESS_KEY="your-aws-secret-key" \
  --name my-bulk-upload-app \
  bulk-products-upload
```

**Command Breakdown:**
*   `-d`: Runs the container in detached mode (in the background).
*   `-p 8080:8080`: Maps port 8080 on your machine to port 8080 in the container.
*   `-e VARIABLE="value"`: Sets the necessary environment variables for the application.
*   `--name my-bulk-upload-app`: Assigns a convenient name to your running container.

### Step 3: How to Use the Endpoints

The application uses **SpringDoc OpenAPI** to generate live, interactive API documentation. This is the best way to explore and test the available endpoints.

1.  **Access the Swagger UI**: Once the container is running, open your web browser and navigate to:
    **http://localhost:8080/swagger-ui.html**

2.  **Explore Endpoints**: The UI will display a list of all available API endpoints (e.g., `POST /api/products/upload`).

3.  **Test an Endpoint**:
    *   Click on an endpoint to expand its details.
    *   Click the **"Try it out"** button.
    *   Fill in the required parameters (e.g., choose a file to upload).
    *   Click the **"Execute"** button to send a live request to your running application.

You will see the `curl` command, the request URL, and the live response from the server directly in your browser.

---

### Managing the Container

*   **View Logs**: `docker logs -f my-bulk-upload-app`
*   **Stop Container**: `docker stop my-bulk-upload-app`
*   **Start Container**: `docker start my-bulk-upload-app`
*   **Remove Container**: `docker rm my-bulk-upload-app`