package com.thinkmart.service;

import com.thinkmart.model.Product;
import com.thinkmart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts(String search, String category, String sort) {
        List<Product> products = productRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(product -> search == null || search.isBlank() || product.getName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .filter(product -> category == null || category.isBlank() || category.equalsIgnoreCase("all") || product.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());

        if (sort == null || sort.isBlank()) {
            return products;
        }

        return switch (sort) {
            case "priceLowHigh" -> products.stream()
                    .sorted(Comparator.comparing((Product product) -> product.getDiscountedPrice()))
                    .collect(Collectors.toList());
            case "priceHighLow" -> products.stream()
                    .sorted(Comparator.comparing((Product product) -> product.getDiscountedPrice()).reversed())
                    .collect(Collectors.toList());
            case "rating" -> products.stream()
                    .sorted(Comparator.comparing(Product::getRating).reversed())
                    .collect(Collectors.toList());
            default -> products;
        };
    }

    public List<String> getCategories() {
        return productRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
    }
}
