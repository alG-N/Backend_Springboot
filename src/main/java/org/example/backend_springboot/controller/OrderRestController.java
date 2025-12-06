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
import java.util.HashMap;
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
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData, Authentication auth) {
        try {
            if (auth == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Unauthorized");
                return ResponseEntity.status(401).body(errorResponse);
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
                Integer productId;
                Integer quantity;

                Object pidObj = item.get("productId");
                if (pidObj instanceof Integer) {
                    productId = (Integer) pidObj;
                } else if (pidObj instanceof Double) {
                    productId = ((Double) pidObj).intValue();
                } else {
                    productId = Integer.parseInt(pidObj.toString());
                }

                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Integer) {
                    quantity = (Integer) qtyObj;
                } else if (qtyObj instanceof Double) {
                    quantity = ((Double) qtyObj).intValue();
                } else {
                    quantity = Integer.parseInt(qtyObj.toString());
                }

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

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedOrder.getId());
            response.put("orderDate", savedOrder.getOrderDate().toString());
            response.put("totalAmount", savedOrder.getTotalAmount());
            response.put("status", savedOrder.getStatus());
            response.put("shippingAddress", savedOrder.getShippingAddress());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}