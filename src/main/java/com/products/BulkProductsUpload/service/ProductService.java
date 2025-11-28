package com.products.BulkProductsUpload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.BulkProductsUpload.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final S3Service s3Service;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ObjectMapper objectMapper;
    private final String bucketName;

    public ProductService(S3Service s3Service, S3Client s3Client, S3Presigner s3Presigner, ObjectMapper objectMapper, @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Service = s3Service;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.objectMapper = objectMapper;
        this.bucketName = bucketName;
    }

    public void uploadProducts(MultipartFile[] files, List<Product> products) throws IOException {
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            MultipartFile file = files[i];

            String folderName = "product-" + UUID.randomUUID();
            String imageUrl = s3Service.uploadFile(file, folderName);
            product.setImageUrl(imageUrl);

            String productJson = objectMapper.writeValueAsString(product);
            s3Service.saveJsonFile(folderName, productJson);
        }
    }

    public List<Product> getAllProducts() throws IOException {
        List<Product> products = new ArrayList<>();
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName).prefix("product-").build();
        List<S3Object> objects = s3Client.listObjectsV2(listObjectsV2Request).contents();

        for (S3Object object : objects) {
            if (object.key().endsWith("product.json")) {
                byte[] objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(object.key()).build()).asByteArray();
                Product product = objectMapper.readValue(objectBytes, Product.class);
                product.setImageUrl(getPresignedUrl(product.getImageUrl()));
                products.add(product);
            }
        }
        return products;
    }

    private String getPresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
    }
}
