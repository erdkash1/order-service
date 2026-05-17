package com.iggy.orderservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long userId;
    private String status;
    private String shippingAddress;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<CartItemDto> items;
}