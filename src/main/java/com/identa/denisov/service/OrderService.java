package com.identa.denisov.service;

import com.identa.denisov.model.Dish;
import com.identa.denisov.model.Order;
import com.identa.denisov.model.OrderStatus;
import com.identa.denisov.model.SelectedDish;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();
    List<Order> getAllOrdersByStatus(OrderStatus status);
    Order createOrder(String description, List<SelectedDish> dishIds);
    Optional<Order> getOrderById(Long id);
    Order saveOrder(Order order);
    List<Dish> getAllDishes();
    void addDishToOrder(Long orderId, Long dishId, int quantity);

}
