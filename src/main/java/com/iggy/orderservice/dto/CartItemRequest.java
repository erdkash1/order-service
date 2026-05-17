package com.iggy.orderservice.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
}