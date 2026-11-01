package com.thinkmart;

import com.thinkmart.model.Product;
import com.thinkmart.repository.ContactMessageRepository;
import com.thinkmart.repository.OrderRepository;
import com.thinkmart.repository.ProductRepository;
import com.thinkmart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ThinkMartApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private CartService cartService;

    @Test
    void productsPageLoads() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("Products"));
    }

    @Test
    void productSearchFiltersResults() throws Exception {
        List<Product> products = productRepository.findByActiveTrueOrderByIdAsc();
        Product first = products.get(0);

        mockMvc.perform(get("/products").param("search", first.getName().substring(0, 4)))
                .andExpect(status().isOk())
                .andExpect(view().name("Products"));

        mockMvc.perform(get("/products").param("category", first.getCategory()))
                .andExpect(status().isOk())
                .andExpect(view().name("Products"));

        mockMvc.perform(get("/products").param("sort", "priceLowHigh").param("category", first.getCategory()))
                .andExpect(status().isOk())
                .andExpect(view().name("Products"));

        assertThat(first.getId()).isNotNull();
    }

    @Test
    void addUpdateAndRemoveCartItems() throws Exception {
        Product product = productRepository.findByActiveTrueOrderByIdAsc().get(0);
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/cart/add/{productId}", product.getId())
                        .session(session)
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        assertThat(cartService.getTotalQuantity(session)).isEqualTo(2);

        mockMvc.perform(post("/cart/update/{productId}", product.getId())
                        .session(session)
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        assertThat(cartService.getTotalQuantity(session)).isEqualTo(3);

        mockMvc.perform(post("/cart/remove/{productId}", product.getId())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        assertThat(cartService.getTotalQuantity(session)).isZero();
    }

    @Test
    void invalidCheckoutRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    void successfulOrderCreation() throws Exception {
        Product product = productRepository.findByActiveTrueOrderByIdAsc().get(0);
        MockHttpSession session = new MockHttpSession();
        int stockBefore = product.getStockQuantity();

        mockMvc.perform(post("/cart/add/{productId}", product.getId())
                        .session(session)
                        .param("quantity", "1"));

        mockMvc.perform(post("/checkout")
                        .session(session)
                        .param("customerName", "Jane Smith")
                        .param("customerEmail", "jane@example.com")
                        .param("phoneNumber", "+1 555 123 4567")
                        .param("deliveryAddress", "123 Demo Street, Sample City"))
                .andExpect(status().is3xxRedirection());

        assertThat(orderRepository.count()).isGreaterThan(0);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity())
                .isEqualTo(stockBefore - 1);
    }

    @Test
    void stockValidationPreventsOverOrder() throws Exception {
        Product product = productRepository.findByActiveTrueOrderByIdAsc().get(0);
        int originalStock = product.getStockQuantity();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/cart/add/{productId}", product.getId())
                        .session(session)
                        .param("quantity", String.valueOf(originalStock + 10)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        assertThat(cartService.getTotalQuantity(session)).isZero();
    }

    @Test
    void contactValidationFailsForBadInput() throws Exception {
        mockMvc.perform(post("/contact")
                        .param("name", "A")
                        .param("email", "invalid-email")
                        .param("message", "too short"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    void contactMessageSuccess() throws Exception {
        mockMvc.perform(post("/contact")
                        .param("name", "Alicia Long")
                        .param("email", "alicia@example.com")
                        .param("message", "We need a quick product recommendation for a home office setup."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact"));

        assertThat(contactMessageRepository.count()).isGreaterThan(0);
    }
}
