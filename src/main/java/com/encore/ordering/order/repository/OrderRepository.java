package com.encore.ordering.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.criteria.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
