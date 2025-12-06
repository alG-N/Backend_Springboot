package org.example.backend_springboot.service;

import org.example.backend_springboot.entity.Order;
import org.example.backend_springboot.entity.OrderDetail;
import org.example.backend_springboot.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Pending");

        // Tính tổng tiền
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail detail : order.getOrderDetails()) {
            detail.setOrder(order);
            BigDecimal itemTotal = detail.getPrice().multiply(new BigDecimal(detail.getQuantity()));
            total = total.add(itemTotal);
        }
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }
}
