# Design Decisions

This document outlines the key architectural and design decisions made during the development of the Bulk Products Upload service. The goal was to create a system that is scalable, secure, and easy for developers to test and review.

---

### 1. Data Persistence Strategy: S3 + DynamoDB

-   **Decision**: Separate storage for images and metadata.
    -   **Images are stored in AWS S3.**
    -   **Product metadata (name, price, etc.) is stored in AWS DynamoDB.**
-   **Rationale**:
    -   **S3** is the industry standard for storing large binary objects. It is cost-effective, highly durable, and scalable. Storing images in a traditional database is inefficient and expensive.
    -   **DynamoDB** is a serverless NoSQL database that offers single-digit millisecond latency. It was chosen over storing JSON files in S3 because it provides powerful querying capabilities, indexing for fast lookups (used for duplicate checks), and strong consistency, which are essential for managing product metadata reliably.

### 2. API Design

-   **Decision**: Provide distinct endpoints for different upload scenarios.
    -   `POST /api/products/upload`: For uploading a single product with one or more images.
    -   `POST /api/products/bulk-upload`: For uploading multiple products, each with a single associated image.
-   **Rationale**: Separating the endpoints provides clear intent and simplifies the backend logic for each use case. It avoids a single, complex endpoint that would need to handle multiple branching scenarios.

-   **Decision**: Implement a paginated `GET` endpoint.
    -   The `GET /api/products` endpoint returns data in a structured, paginated format that includes `total`, `page`, `pageSize`, and `totalPages`.
-   **Rationale**: Returning all products in a single request is not scalable and would lead to performance degradation as the dataset grows. Pagination ensures fast, predictable response times and simplifies UI development on the frontend.

### 3. Security

-   **Decision**: Use private S3 buckets with pre-signed URLs.
-   **Rationale**: All product images are stored in a private S3 bucket, preventing direct public access. When a client requests product data, the application generates temporary, secure, time-limited URLs (pre-signed URLs) for the images. This is a standard security pattern that ensures only authorized users can view the assets.

-   **Decision**: Use the default AWS credential provider chain.
-   **Rationale**: The application does not handle raw access keys or secrets in its configuration files. It relies on the AWS SDK's default provider chain, which can automatically source credentials from environment variables (in CI/CD), EC2 instance profiles (in production), or a local `~/.aws/credentials` file (for local development). This is the most secure and flexible approach.

### 4. Local Development & Reviewability

-   **Decision**: Integrate LocalStack using Docker Compose and Spring Profiles.
-   **Rationale**: To allow developers and reviewers to run the entire application stack locally without needing a real AWS account, the project is configured to connect to LocalStack.
    -   A `docker-compose.yml` file makes starting the local AWS environment a one-command operation.
    -   A Spring Profile named `local` cleanly separates the configuration for connecting to LocalStack from the production configuration. This is activated via a command-line argument, ensuring no code changes are needed to switch between environments.
    -   This design makes the project highly portable and dramatically simplifies the review and testing process.

### 5. Data Integrity

-   **Decision**: Implement server-side validation for duplicate products.
-   **Rationale**: To prevent data corruption, the service validates incoming products before performing any write operations.
    -   A **Global Secondary Index (GSI)** was created on the `name` attribute in the DynamoDB table to allow for efficient duplicate lookups.
    -   For bulk uploads, the entire batch is validated for internal duplicates and against the database before any data is saved, making the operation more transactional.
    -   This server-side check ensures data integrity regardless of the client.
