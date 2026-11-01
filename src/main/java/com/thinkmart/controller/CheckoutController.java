package com.thinkmart.controller;

import com.thinkmart.dto.CheckoutRequest;
import com.thinkmart.model.Order;
import com.thinkmart.service.CartService;
import com.thinkmart.service.CheckoutService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final CheckoutService checkoutService;

    public CheckoutController(CartService cartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        if (cartService.isEmpty(session)) {
            model.addAttribute("errorMessage", "Your cart is empty. Add some products before checkout.");
            return "redirect:/products";
        }

        model.addAttribute("checkoutRequest", new CheckoutRequest());
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("subtotal", cartService.getSubtotal(session));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
        return "Checkout";
    }

    @PostMapping("/checkout")
    public String submitCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequest checkoutRequest,
                                 BindingResult result,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (cartService.isEmpty(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add products before checkout.");
            return "redirect:/products";
        }

        if (result.hasErrors()) {
            model.addAttribute("items", cartService.getItems(session));
            model.addAttribute("subtotal", cartService.getSubtotal(session));
            model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
            return "Checkout";
        }

        try {
            Order order = checkoutService.placeOrder(session, checkoutRequest);
            return "redirect:/checkout/confirmation/" + order.getOrderNumber();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("items", cartService.getItems(session));
            model.addAttribute("subtotal", cartService.getSubtotal(session));
            model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
            return "Checkout";
        }
    }

    @GetMapping("/checkout/confirmation/{orderNumber}")
    public String confirmation(@PathVariable String orderNumber, Model model) {
        model.addAttribute("orderNumber", orderNumber);
        return "OrderConfirmation";
    }
}
