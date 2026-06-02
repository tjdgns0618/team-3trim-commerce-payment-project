package com.example.team3trimcommercepaymentproject.domain.order.controller;

import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCancelRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderPreviewRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCancelResponse;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderDetailResponse;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderPageResponse;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderPreviewResponse;
import com.example.team3trimcommercepaymentproject.domain.order.service.OrderService;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @RequestBody OrderPreviewRequest request
    ) {
        Long memberId = getLoginMemberId();

        OrderPreviewResponse response = orderService.preview(memberId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문/결제 동시 생성
     */
    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(
            @RequestBody OrderCreateRequest request
    ) {
        Long memberId = getLoginMemberId();

        OrderCreateResponse response = orderService.create(memberId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 주문 내역 조회
     */
    @GetMapping
    public ResponseEntity<OrderPageResponse> findOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long memberId = getLoginMemberId();

        OrderPageResponse response = orderService.findOrders(memberId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> findOrderDetail(
            @PathVariable Long orderId
    ) {
        Long memberId = getLoginMemberId();

        OrderDetailResponse response = orderService.findByIdOrder(memberId, orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 취소
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderCancelResponse> cancel(
            @PathVariable Long orderId,
            @RequestBody OrderCancelRequest request
    ) {
        Long memberId = getLoginMemberId();

        OrderCancelResponse response = orderService.cancel(memberId, orderId, request);

        return ResponseEntity.ok(response);
    }

    private Long getLoginMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Long memberId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return memberId;
    }
}