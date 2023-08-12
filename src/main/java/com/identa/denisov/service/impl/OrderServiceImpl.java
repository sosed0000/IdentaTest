package com.identa.denisov.service.impl;

import com.identa.denisov.controller.WebSocketController;
import com.identa.denisov.model.Dish;
import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;
import com.identa.denisov.repository.DishRepository;
import com.identa.denisov.repository.OrderRepository;
import com.identa.denisov.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, DishRepository dishRepository) {
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
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
        order.setDishes(new ArrayList<>());
        orderRepository.save(order);
        logger.info("New order created with id: {}", order.getId());
        logAllOrders();
        return order;
    }
    @Override
    @Transactional
    public Order createOrderWithDishes(String description, List<Long> dishIds) {
        Order order = new Order();
        order.setDescription(description);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);
        order.setDishes(new ArrayList<>());

        for (Long dishId : dishIds) {
            Dish dish = dishRepository.findById(dishId).orElse(null);
            if (dish != null) {
                dish.setOrder(order);
                order.getDishes().add(dish);
            }
        }

        orderRepository.save(order);

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

    @Override
    public void addDishToOrder(Long orderId, Dish dish) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            dish.setOrder(order);
            order.getDishes().add(dish);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    @Override
    public void removeDishFromOrder(Long orderId, Long dishId) {
        Order order = getOrderById(orderId).orElse(null);
//        if (order != null) {
//            Dish dishToRemove = order.getDishes().stream()
//                    .filter(dish -> dish.getId().equals(dishId))
//                    .findFirst()
//                    .orElse(null);
//            if (dishToRemove != null) {
//                order.getDishes().remove(dishToRemove);
//                saveOrder(order);
//            }
//        }
    }
}
