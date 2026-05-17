package com.iggy.orderservice.service;

import com.iggy.orderservice.client.ProductClient;
import com.iggy.orderservice.dto.CartItemDto;
import com.iggy.orderservice.dto.OrderRequest;
import com.iggy.orderservice.dto.OrderResponse;
import com.iggy.orderservice.entity.Cart;
import com.iggy.orderservice.entity.Order;
import com.iggy.orderservice.entity.OrderItem;
import com.iggy.orderservice.entity.OrderStatus;
import com.iggy.orderservice.repository.CartRepository;
import com.iggy.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartService cartService,
                        ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Reduce stock for each item via Product Service
        for (var item : cart.getItems()) {
            boolean success = productClient.reduceStock(
                    item.getProductId(), item.getQuantity());
            if (!success) {
                throw new RuntimeException("Insufficient stock for product: "
                        + item.getProductName());
            }
        }

        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            return orderItem;
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        order.setTotalAmount(orderItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum());

        Order savedOrder = orderRepository.save(order);

        // Clear cart after order placed
        cartService.clearCart(request.getUserId());

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.valueOf(status));
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setShippingAddress(order.getShippingAddress());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            List<CartItemDto> items = order.getItems().stream().map(item -> {
                CartItemDto dto = new CartItemDto();
                dto.setId(item.getId());
                dto.setProductId(item.getProductId());
                dto.setProductName(item.getProductName());
                dto.setPrice(item.getPrice());
                dto.setQuantity(item.getQuantity());
                return dto;
            }).collect(Collectors.toList());
            response.setItems(items);
        }

        return response;
    }
}