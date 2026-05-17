package com.iggy.orderservice.service;

import com.iggy.orderservice.dto.CartItemDto;
import com.iggy.orderservice.dto.CartItemRequest;
import com.iggy.orderservice.dto.CartResponse;
import com.iggy.orderservice.entity.Cart;
import com.iggy.orderservice.entity.CartItem;
import com.iggy.orderservice.repository.CartItemRepository;
import com.iggy.orderservice.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(request.getProductId());
        item.setProductName(request.getProductName());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        // Manually add to cart items list
        cart.getItems().add(item);
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return mapToResponse(cart);
    }

    public CartResponse removeItem(Long userId, Long itemId) {
        cartItemRepository.deleteById(itemId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return mapToResponse(cart);
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());

        List<CartItemDto> items = cart.getItems() == null ? List.of() :
                cart.getItems().stream().map(item -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setId(item.getId());
                    dto.setProductId(item.getProductId());
                    dto.setProductName(item.getProductName());
                    dto.setPrice(item.getPrice());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                }).collect(Collectors.toList());

        response.setItems(items);
        response.setTotalAmount(items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum());
        return response;
    }
}