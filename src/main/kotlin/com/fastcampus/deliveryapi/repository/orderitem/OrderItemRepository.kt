package com.fastcampus.deliveryapi.repository.orderitem

import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository: JpaRepository<OrderItem, Long>