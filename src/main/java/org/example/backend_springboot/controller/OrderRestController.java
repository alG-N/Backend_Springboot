package org.example.backend_springboot.controller;

import org.example.backend_springboot.entity.Order;
import org.example.backend_springboot.entity.OrderDetail;
import org.example.backend_springboot.entity.Product;
import org.example.backend_springboot.entity.User;
import org.example.backend_springboot.service.OrderService;
import org.example.backend_springboot.service.ProductService;
import org.example.backend_springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(orderService.getOrdersByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        Order order = orderService.getOrderById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> orderData, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress((String) orderData.get("shippingAddress"));

        List<OrderDetail> details = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");

        for (Map<String, Object> item : items) {
            Integer productId = (Integer) item.get("productId");
            Integer quantity = (Integer) item.get("quantity");

            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setPrice(product.getPrice());
            details.add(detail);
        }

        order.setOrderDetails(details);
        Order savedOrder = orderService.createOrder(order);

        return ResponseEntity.ok(savedOrder);
    }
}
