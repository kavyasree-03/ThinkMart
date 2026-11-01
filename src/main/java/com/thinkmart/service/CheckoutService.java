package com.thinkmart.service;

import com.thinkmart.dto.CheckoutRequest;
import com.thinkmart.model.Order;
import com.thinkmart.model.OrderItem;
import com.thinkmart.model.Product;
import com.thinkmart.repository.OrderItemRepository;
import com.thinkmart.repository.OrderRepository;
import com.thinkmart.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CheckoutService(CartService cartService, ProductRepository productRepository,
                          OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order placeOrder(HttpSession session, CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Checkout request is missing.");
        }

        Map<Long, Integer> cart = cartService.getCartMap(session);
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty. Add items before checkout.");
        }

        String customerName = request.getCustomerName() == null ? "" : request.getCustomerName().trim();
        String customerEmail = request.getCustomerEmail() == null ? "" : request.getCustomerEmail().trim();
        String phoneNumber = request.getPhoneNumber() == null ? "" : request.getPhoneNumber().trim();
        String deliveryAddress = request.getDeliveryAddress() == null ? "" : request.getDeliveryAddress().trim();

        if (customerName.isBlank() || customerEmail.isBlank() || phoneNumber.isBlank() || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("All checkout fields are required.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Invalid quantity set for a cart item.");
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("One or more products are no longer available."));

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product " + product.getName() + " is unavailable.");
            }

            if (quantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getName() + ".");
            }

            subtotal = subtotal.add(product.getDiscountedPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setPhoneNumber(phoneNumber);
        order.setDeliveryAddress(deliveryAddress);
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal);
        order.setStatus("PLACED");

        Order savedOrder = orderRepository.save(order);

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("One or more products are no longer available."));

            BigDecimal unitPrice = product.getDiscountedPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(entry.getValue()));

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(unitPrice);
            item.setQuantity(entry.getValue());
            item.setSubtotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
            savedOrder.getItems().add(item);

            product.setStockQuantity(product.getStockQuantity() - entry.getValue());
            productRepository.save(product);
            orderItemRepository.save(item);
        }

        orderRepository.save(savedOrder);
        cartService.clearCart(session);
        return savedOrder;
    }
}
