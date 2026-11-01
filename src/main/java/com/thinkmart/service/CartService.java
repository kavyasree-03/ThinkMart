package com.thinkmart.service;

import com.thinkmart.model.Product;
import com.thinkmart.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final ProductRepository productRepository;

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Map<Long, Integer> getCartMap(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    public void addItem(HttpSession session, Long productId, Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (!product.isActive()) {
            throw new IllegalArgumentException("This product is currently unavailable.");
        }

        Map<Long, Integer> cart = getCartMap(session);
        int existingQuantity = cart.getOrDefault(productId, 0);
        int requestedTotal = existingQuantity + requestedQuantity;

        if (requestedTotal > product.getStockQuantity()) {
            throw new IllegalArgumentException("Only " + product.getStockQuantity() + " units are currently available.");
        }

        cart.put(productId, requestedTotal);
        session.setAttribute("cart", cart);
    }

    public void updateQuantity(HttpSession session, Long productId, Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (!product.isActive()) {
            throw new IllegalArgumentException("This product is no longer available.");
        }

        if (requestedQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Only " + product.getStockQuantity() + " units are available in stock.");
        }

        Map<Long, Integer> cart = getCartMap(session);
        cart.put(productId, requestedQuantity);
        session.setAttribute("cart", cart);
    }

    public void removeItem(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.remove(productId);
        session.setAttribute("cart", cart);
    }

    public boolean isEmpty(HttpSession session) {
        return getCartMap(session).isEmpty();
    }

    public void clearCart(HttpSession session) {
        session.setAttribute("cart", new LinkedHashMap<>());
    }

    public List<CartItemView> getItems(HttpSession session) {
        Map<Long, Integer> cart = getCartMap(session);
        List<CartItemView> items = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product == null || !product.isActive()) {
                continue;
            }

            int quantity = entry.getValue();
            BigDecimal unitPrice = product.getDiscountedPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            items.add(new CartItemView(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    unitPrice,
                    quantity,
                    subtotal
            ));
        }

        return items;
    }

    public int getTotalQuantity(HttpSession session) {
        return getCartMap(session).values().stream().mapToInt(Integer::intValue).sum();
    }

    public BigDecimal getSubtotal(HttpSession session) {
        return getItems(session).stream()
                .map(CartItemView::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getOrderTotal(HttpSession session) {
        return getSubtotal(session);
    }

    public int getCount(HttpSession session) {
        return getTotalQuantity(session);
    }

    public static class CartItemView {
        private final Long productId;
        private final String productName;
        private final String imageUrl;
        private final BigDecimal unitPrice;
        private final int quantity;
        private final BigDecimal subtotal;

        public CartItemView(Long productId, String productName, String imageUrl, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }
}
