package com.fastcampus.deliveryapi.repository.order

import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository: JpaRepository<Order, Long> {
    fun existsByCheckoutId(checkoutId: Long): Boolean
}