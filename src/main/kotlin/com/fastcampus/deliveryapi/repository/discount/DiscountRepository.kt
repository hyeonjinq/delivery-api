package com.fastcampus.deliveryapi.repository.discount

import org.springframework.data.jpa.repository.JpaRepository

interface DiscountRepository : JpaRepository<Discount, Long> {
    fun findAllByDiscountIdIn(discountIds: List<Long>): List<Discount>
}