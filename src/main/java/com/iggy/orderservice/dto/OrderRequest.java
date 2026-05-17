package com.iggy.orderservice.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private String shippingAddress;
}