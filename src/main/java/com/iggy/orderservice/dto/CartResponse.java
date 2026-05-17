package com.iggy.orderservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private Double totalAmount;
}