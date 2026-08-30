package com.booking.resource.dto;

import java.math.BigDecimal;

public class ResourceResponse {

    private Long id;
    private String name;
    private String type;
    private String description;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private Boolean available;

    public ResourceResponse() {
    }

    public ResourceResponse(Long id, String name, String type, String description, Integer capacity, BigDecimal pricePerHour, Boolean available) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
