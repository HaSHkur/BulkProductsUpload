package com.products.BulkProductsUpload.dto;

import com.products.BulkProductsUpload.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedProductResponse {
    private List<Product> data;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
}
