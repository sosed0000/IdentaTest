package com.identa.denisov.service.impl;

import com.identa.denisov.controller.WebSocketController;
import com.identa.denisov.model.*;
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
    public Order createOrderWithDishes(String description, List<SelectedDish> selectedDishes) {
        Order order = new Order();
        order.setDescription(description);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);

        List<OrderedDish> orderedDishes = new ArrayList<>();

        for (SelectedDish selectedDish : selectedDishes) {
            Dish dish = dishRepository.findById(selectedDish.getDishId()).orElse(null);
            if (dish != null) {
                OrderedDish orderedDish = new OrderedDish();
                orderedDish.setDish(dish);
                orderedDish.setQuantity(selectedDish.getQuantity());
                orderedDishes.add(orderedDish);
                dishRepository.save(dish);
            }
        }

        order.setOrderedDishes(orderedDishes);
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
    public void addDishToOrder(Long orderId, Long dishId, int quantity) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        Optional<Dish> optionalDish = dishRepository.findById(dishId);

        if (optionalOrder.isPresent() && optionalDish.isPresent()) {
            Order order = optionalOrder.get();
            Dish dish = optionalDish.get();

            boolean dishAlreadyExists = false;
            for (OrderedDish existingDish : order.getOrderedDishes()) {
                if (existingDish.getDish().getId().equals(dishId)) {
                    existingDish.setQuantity(existingDish.getQuantity() + quantity);
                    dishAlreadyExists = true;
                    break;
                }
            }

            if (!dishAlreadyExists) {
                OrderedDish orderedDish = new OrderedDish();
                orderedDish.setOrder(order);
                orderedDish.setDish(dish);
                orderedDish.setQuantity(quantity);

                order.getOrderedDishes().add(orderedDish);
            }

            orderRepository.save(order);
        }
    }

    @Override
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }


}
