package com.example.team3trimcommercepaymentproject.domain.order.repository;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
