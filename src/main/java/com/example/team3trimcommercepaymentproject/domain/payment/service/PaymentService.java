package com.example.team3trimcommercepaymentproject.domain.payment.service;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.member.repository.MemberRepository;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderPreviewItemResponse;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import com.example.team3trimcommercepaymentproject.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final JwtProvider jwtProvider;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository

    /**
     * 주문/결제 동시 생성
     **/
    public OrderCreateResponse createPaymemt(Long menberId, OrderCreateRequest request) {

        Cart cart = cartRepository.findByMemberId(menberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));


        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        List<Long> cartItemIds = request.cartItemIds();

        List<CartItem> targetCartItems;

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            targetCartItems = cartItems;
        } else {
            targetCartItems = cartItems.stream()
                    .filter(cartItem -> cartItemIds.contains(cartItem.getId()))
                    .toList();

            if (targetCartItems.isEmpty() || targetCartItems.size() != cartItemIds.size()) {
                throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
            }
        }
        for (CartItem cartItem : targetCartItems) {
            Product product = cartItem.getProduct();

            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

            Long price = product.getPrice().longValue();
            Integer quantity = cartItem.getQuantity();
            Long subtotalAmount = price * quantity;

            OrderPreviewItemResponse previewItem = new OrderPreviewItemResponse(
                    cartItem.getId(),
                    product.getId(),
                    product.getName(),
                    price,
                    quantity,
                    subtotalAmount
            );

            String portonePaymentId =genertePortonePaymentId();
    }
}
