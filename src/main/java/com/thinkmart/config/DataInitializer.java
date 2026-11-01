package com.thinkmart.config;

import com.thinkmart.model.Product;
import com.thinkmart.repository.ProductRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }

        Product laptop = new Product();
        laptop.setName("AeroBook Pro 14");
        laptop.setDescription("Ultralight productivity laptop for work, study, and travel.");
        laptop.setCategory("Laptops");
        laptop.setImageUrl("https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80");
        laptop.setOriginalPrice(new BigDecimal("1399.00"));
        laptop.setDiscountPercentage(15);
        laptop.setRating(new BigDecimal("4.8"));
        laptop.setStockQuantity(18);
        laptop.setActive(true);

        Product mobile = new Product();
        mobile.setName("Nova X Smartphone");
        mobile.setDescription("Flagship everyday smartphone with premium camera and battery life.");
        mobile.setCategory("Mobiles");
        mobile.setImageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80");
        mobile.setOriginalPrice(new BigDecimal("899.00"));
        mobile.setDiscountPercentage(12);
        mobile.setRating(new BigDecimal("4.7"));
        mobile.setStockQuantity(42);
        mobile.setActive(true);

        Product watch = new Product();
        watch.setName("Orbit Smart Watch");
        watch.setDescription("Fitness-first smartwatch with GPS, heart rate monitoring, and AMOLED display.");
        watch.setCategory("Watches");
        watch.setImageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80");
        watch.setOriginalPrice(new BigDecimal("249.00"));
        watch.setDiscountPercentage(18);
        watch.setRating(new BigDecimal("4.6"));
        watch.setStockQuantity(29);
        watch.setActive(true);

        Product headphone = new Product();
        headphone.setName("Zen Audio Pro");
        headphone.setDescription("Noise-cancelling wireless headphones tuned for immersive listening.");
        headphone.setCategory("Headphones");
        headphone.setImageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=900&q=80");
        headphone.setOriginalPrice(new BigDecimal("329.00"));
        headphone.setDiscountPercentage(20);
        headphone.setRating(new BigDecimal("4.9"));
        headphone.setStockQuantity(23);
        headphone.setActive(true);

        Product camera = new Product();
        camera.setName("PixelStream Camera");
        camera.setDescription("Compact mirrorless camera built for creators and travelers.");
        camera.setCategory("Cameras");
        camera.setImageUrl("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=900&q=80");
        camera.setOriginalPrice(new BigDecimal("1199.00"));
        camera.setDiscountPercentage(10);
        camera.setRating(new BigDecimal("4.5"));
        camera.setStockQuantity(14);
        camera.setActive(true);

        Product accessory = new Product();
        accessory.setName("DeskMate Accessory Kit");
        accessory.setDescription("Everyday essentials bundle with charging cable, sleeve, and keyboard tray.");
        accessory.setCategory("Accessories");
        accessory.setImageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=900&q=80");
        accessory.setOriginalPrice(new BigDecimal("129.00"));
        accessory.setDiscountPercentage(15);
        accessory.setRating(new BigDecimal("4.4"));
        accessory.setStockQuantity(77);
        accessory.setActive(true);

        productRepository.saveAll(List.of(laptop, mobile, watch, headphone, camera, accessory));
    }
}
