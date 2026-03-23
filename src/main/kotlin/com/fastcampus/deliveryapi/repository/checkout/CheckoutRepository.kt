package com.fastcampus.deliveryapi.repository.checkout

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CheckoutRepository: JpaRepository<Checkout, Long> {
    fun findAllByCheckoutIdIsNotAndCustomerIdIs(checkoutId: Long, customerId: Long): List<Checkout>
}