package com.products.BulkProductsUpload.service;

import com.products.BulkProductsUpload.exception.DuplicateProductException;
import com.products.BulkProductsUpload.model.Product;
import com.products.BulkProductsUpload.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;


import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final S3Service s3Service;
    private final ProductRepository productRepository;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public ProductService(S3Service s3Service, ProductRepository productRepository, S3Presigner s3Presigner, @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Service = s3Service;
        this.productRepository = productRepository;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public void uploadProduct(MultipartFile[] files, Product product) throws IOException {
        // 1. Check for duplicates
        productRepository.findByName(product.getName()).ifPresent(p -> {
            throw new DuplicateProductException("A product with the name '" + product.getName() + "' already exists.");
        });

        // 2. Upload images to S3
        String productId = UUID.randomUUID().toString();
        String folderName = "product-" + productId;
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String s3key = s3Service.uploadFile(file, folderName);
            imageUrls.add(s3key);
        }
        
        // 3. Save metadata to DynamoDB
        product.setId(productId);
        product.setImageUrls(imageUrls);
        productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        
        // Generate pre-signed URLs for each image
        products.forEach(product -> {
            List<String> presignedUrls = product.getImageUrls().stream()
                    .map(this::getPresignedUrl)
                    .collect(Collectors.toList());
            product.setImageUrls(presignedUrls);
        });
        
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
