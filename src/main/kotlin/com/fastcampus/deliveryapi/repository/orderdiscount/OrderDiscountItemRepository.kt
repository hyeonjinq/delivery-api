package com.fastcampus.deliveryapi.repository.orderdiscount

import org.springframework.data.jpa.repository.JpaRepository

interface OrderDiscountItemRepository : JpaRepository<OrderDiscountItem, Long> {
    fun findAllByOrderId(orderId: Long): List<OrderDiscountItem>
}