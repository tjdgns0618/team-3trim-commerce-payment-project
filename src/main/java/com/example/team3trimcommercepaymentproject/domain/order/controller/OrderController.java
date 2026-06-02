package com.example.team3trimcommercepaymentproject.domain.order.controller;

import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCancelRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderPreviewRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.*;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문서 미리보기
     */
    @PostMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> preview(
            @AuthenticationPrincipal Long memberId,
            @RequestBody OrderPreviewRequest request
    ) {
        OrderPreviewResponse response = orderService.preview(memberId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문/결제 동시 생성
     */
    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(
            @AuthenticationPrincipal Long memberId,
            @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.create(memberId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 주문 내역 조회
     */
    @GetMapping
    public ResponseEntity<OrderPageResponse> findOrders(
            @AuthenticationPrincipal Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        OrderPageResponse response = orderService.findOrders(memberId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> findOrderDetail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse response = orderService.findByIdOrder(memberId, orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 취소
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderCancelResponse> cancel(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId,
            @RequestBody OrderCancelRequest request
    ) {
        OrderCancelResponse response = orderService.cancel(memberId, orderId, request);

        return ResponseEntity.ok(response);
    }
}