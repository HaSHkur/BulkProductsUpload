package com.products.BulkProductsUpload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@Profile("local") // This configuration is active ONLY when the 'local' profile is active
public class AwsLocalConfig {

    @Bean
    public S3Client s3Client(@Value("${aws.region}") String region, @Value("${aws.endpoint}") URI endpoint) {
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(endpoint)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient(@Value("${aws.region}") String region, @Value("${aws.endpoint}") URI endpoint) {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .endpointOverride(endpoint)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${aws.region}") String region, @Value("${aws.endpoint}") URI endpoint) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .endpointOverride(endpoint)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
    }
}
