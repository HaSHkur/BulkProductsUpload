package com.products.BulkProductsUpload.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.BulkProductsUpload.model.Product;
import com.products.BulkProductsUpload.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public ProductController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadProducts(@RequestPart("files") MultipartFile[] files,
                                 @RequestPart("products") String productsJson) throws IOException {
        List<Product> products = objectMapper.readValue(productsJson, new TypeReference<List<Product>>() {});
        productService.uploadProducts(files, products);
        return "Products uploaded successfully";
    }

    @GetMapping
    public List<Product> getAllProducts() throws IOException {
        return productService.getAllProducts();
    }
}
