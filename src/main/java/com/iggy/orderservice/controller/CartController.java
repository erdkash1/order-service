package com.iggy.orderservice.controller;

import com.iggy.orderservice.dto.CartItemRequest;
import com.iggy.orderservice.dto.CartResponse;
import com.iggy.orderservice.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }
}