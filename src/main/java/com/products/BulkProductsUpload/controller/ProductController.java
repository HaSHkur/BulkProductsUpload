package com.products.BulkProductsUpload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.BulkProductsUpload.model.Product;
import com.products.BulkProductsUpload.service.ProductService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProductController(ProductService productService, ObjectMapper objectMapper, Validator validator) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadProduct(@RequestPart("files") MultipartFile[] files,
                                @RequestPart("product") String productJson) throws IOException {
        Product product = objectMapper.readValue(productJson, Product.class);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        productService.uploadProduct(files, product);
        return "Product uploaded successfully";
    }

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        boolean noPagination = (page == null && pageSize == null);

        if (noPagination) {
            return productService.getAllProducts();
        }

        int p = (page != null) ? page : 1;
        int size = (pageSize != null) ? pageSize : 20;

        return productService.getProductsPaginated(p, size);
    }

}
