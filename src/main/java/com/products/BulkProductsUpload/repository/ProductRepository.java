package com.products.BulkProductsUpload.repository;

import com.products.BulkProductsUpload.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);
    public static final String TABLE_NAME = "Products";
    public static final String NAME_INDEX = "name-index";
    private final DynamoDbTable<Product> productTable;
    private final DynamoDbIndex<Product> nameIndex;

    public ProductRepository(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient enhancedClient) {
        this.productTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Product.class));
        this.nameIndex = productTable.index(NAME_INDEX);

        try {
            logger.info("Attempting to create DynamoDB table '{}' if it doesn't exist...", TABLE_NAME);
            CreateTableEnhancedRequest request = CreateTableEnhancedRequest.builder()
                    .globalSecondaryIndices(gsi -> gsi.indexName(NAME_INDEX)
                            .projection(p -> p.projectionType(ProjectionType.ALL))
                    )
                    .build();
            productTable.createTable(request);

            logger.info("Table creation request sent. Waiting for table to become active...");
            DynamoDbWaiter dbWaiter = dynamoDbClient.waiter();
            DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(TABLE_NAME).build();
            dbWaiter.waitUntilTableExists(tableRequest);
            logger.info("Table '{}' is active.", TABLE_NAME);

        } catch (ResourceInUseException e) {
            logger.info("Table '{}' already exists. Skipping creation.", TABLE_NAME);
        } catch (Exception e) {
            logger.error("FATAL: Failed to connect to or set up DynamoDB.", e);
            throw new RuntimeException("Could not initialize ProductRepository. Please check DynamoDB/LocalStack connection.", e);
        }
    }

    public void save(Product product) {
        productTable.putItem(product);
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(productTable.getItem(Key.builder().partitionValue(id).build()));
    }

    public Optional<Product> findByName(String name) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(name).build());
        return nameIndex.query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    public List<Product> findAll() {
        return productTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }
}
