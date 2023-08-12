package com.identa.denisov.dto;

import java.util.List;

public class CreateOrderRequest {
    private String description;
    private List<Long> dishIds;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getDishIds() {
        return dishIds;
    }

    public void setDishIds(List<Long> dishIds) {
        this.dishIds = dishIds;
    }
}
