package com.thinkmart.controller;

import com.thinkmart.model.Product;
import com.thinkmart.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String search,
                           @RequestParam(required = false, defaultValue = "all") String category,
                           @RequestParam(required = false, defaultValue = "nameAsc") String sort,
                           Model model) {
        List<Product> products = productService.getProducts(search, category, sort);

        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", productService.getCategories());
        return "Products";
    }
}
