package com.products.BulkProductsUpload.service;

import com.products.BulkProductsUpload.dto.PaginatedProductResponse;
import com.products.BulkProductsUpload.exception.DuplicateProductException;
import com.products.BulkProductsUpload.exception.ProductNotFoundException;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        productRepository.findByName(product.getName()).ifPresent(p -> {
            throw new DuplicateProductException("A product with the name '" + product.getName() + "' already exists.");
        });

        String productId = UUID.randomUUID().toString();
        String folderName = "product-" + productId;
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String s3key = s3Service.uploadFile(file, folderName);
            imageUrls.add(s3key);
        }
        
        product.setId(productId);
        product.setImageUrls(imageUrls);
        productRepository.save(product);
    }

    public void uploadBulkProducts(MultipartFile[] files, List<Product> products) throws IOException {
        Set<String> productNamesInBatch = new HashSet<>();
        for (Product product : products) {
            if (!productNamesInBatch.add(product.getName())) {
                throw new DuplicateProductException("The bulk request contains duplicate product names: '" + product.getName() + "'.");
            }
        }

        for (Product product : products) {
            productRepository.findByName(product.getName()).ifPresent(p -> {
                throw new DuplicateProductException("A product with the name '" + product.getName() + "' already exists in the database.");
            });
        }

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            MultipartFile file = files[i];

            String productId = UUID.randomUUID().toString();
            String folderName = "product-" + productId;
            
            String s3key = s3Service.uploadFile(file, folderName);
            
            product.setId(productId);
            product.setImageUrls(Collections.singletonList(s3key));
            productRepository.save(product);
        }
    }

    public Product getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        List<String> presignedUrls = product.getImageUrls().stream()
                .map(this::getPresignedUrl)
                .collect(Collectors.toList());
        product.setImageUrls(presignedUrls);
        
        return product;
    }

    public PaginatedProductResponse getProductsPaginated(int page, int pageSize) {
        List<Product> allProducts = productRepository.findAll();
        long totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= totalProducts) {
            return new PaginatedProductResponse(Collections.emptyList(), totalProducts, page, pageSize, totalPages);
        }

        int toIndex = Math.min(fromIndex + pageSize, (int) totalProducts);
        List<Product> paginatedList = allProducts.subList(fromIndex, toIndex);

        paginatedList.forEach(p -> {
            List<String> presignedUrls = p.getImageUrls().stream()
                    .map(this::getPresignedUrl)
                    .collect(Collectors.toList());
            p.setImageUrls(presignedUrls);
        });
        
        return new PaginatedProductResponse(paginatedList, totalProducts, page, pageSize, totalPages);
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
