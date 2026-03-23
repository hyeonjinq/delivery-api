package com.fastcampus.deliveryapi.repository.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    fun findAllByCustomerIdAndIsDeleted(customerId: Long, isDelete: Boolean): Optional<Cart>
}