package org.example.backend_springboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        Map<Integer, CartItem> cart = getCartFromSession(session);

        List<CartItem> items = new ArrayList<>(cart.values());
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartItem item, HttpSession session) {
        Map<Integer, CartItem> cart = getCartFromSession(session);

        if (cart.containsKey(item.getProductId())) {
            CartItem existing = cart.get(item.getProductId());
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            cart.put(item.getProductId(), item);
        }

        session.setAttribute("cart", cart);
        return ResponseEntity.ok("Added to cart");
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<String> updateCart(@PathVariable Integer productId, @RequestBody Map<String, Integer> body, HttpSession session) {
        Map<Integer, CartItem> cart = getCartFromSession(session);

        if (cart.containsKey(productId)) {
            cart.get(productId).setQuantity(body.get("quantity"));
        }

        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Integer productId, HttpSession session) {
        Map<Integer, CartItem> cart = getCartFromSession(session);
        cart.remove(productId);
        return ResponseEntity.ok("Removed");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return ResponseEntity.ok("Cart cleared");
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, CartItem> getCartFromSession(HttpSession session) {
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    public static class CartItem {
        private Integer productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String image;

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }
}
