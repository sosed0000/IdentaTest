package com.identa.denisov.dto;

import com.identa.denisov.model.SelectedDish;

import java.util.List;

public class CreateOrderRequest {
    private String description;
    private List<SelectedDish> selectedDishes;
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SelectedDish> getSelectedDishes() {
        return selectedDishes;
    }

    public void setSelectedDishes(List<SelectedDish> selectedDishes) {
        this.selectedDishes = selectedDishes;
    }
}
