package com.identa.denisov.service.impl;

import com.identa.denisov.controller.WebSocketController;
import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;
import com.identa.denisov.repository.OrderRepository;
import com.identa.denisov.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order createOrder(String description) {
        logger.info("Creating new order with description: {}", description);
        Order order = new Order();
        order.setDescription(description);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);
        orderRepository.save(order);
        logger.info("New order created with id: {}", order.getId());
        logAllOrders();
        return order;
    }
    private void logAllOrders() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            logger.info("Order id: {}, description: {}, status: {}", order.getId(), order.getDescription(), order.getStatus());
        }
    }


    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status);
    }
}
