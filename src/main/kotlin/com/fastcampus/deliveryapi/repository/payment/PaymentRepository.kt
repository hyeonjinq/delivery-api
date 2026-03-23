package com.fastcampus.deliveryapi.repository.payment

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long>{
}