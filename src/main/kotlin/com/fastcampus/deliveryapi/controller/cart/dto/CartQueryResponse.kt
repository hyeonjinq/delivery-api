package com.fastcampus.deliveryapi.controller.cart.dto

data class CartQueryResponse(
    val customerId: Long,
    val cartItems: List<CartMenuDTO>
)
