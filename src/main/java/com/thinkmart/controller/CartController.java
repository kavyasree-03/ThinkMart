package com.thinkmart.controller;

import com.thinkmart.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("subtotal", cartService.getSubtotal(session));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
        model.addAttribute("orderTotal", cartService.getOrderTotal(session));
        return "Cart";
    }

    @PostMapping("/cart/add/{productId}")
    public String addItem(@PathVariable Long productId,
                          @RequestParam(defaultValue = "1") Integer quantity,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            cartService.addItem(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/cart/update/{productId}")
    public String updateItem(@PathVariable Long productId,
                             @RequestParam Integer quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{productId}")
    public String removeItem(@PathVariable Long productId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.removeItem(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }
}
