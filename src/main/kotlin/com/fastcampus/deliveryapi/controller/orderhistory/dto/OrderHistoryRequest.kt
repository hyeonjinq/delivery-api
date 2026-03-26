package com.fastcampus.deliveryapi.controller.orderhistory.dto

import com.fastcampus.deliveryapi.domain.order.OrderStatus

data class OrderHistoryRequest(
    val customerId: Long,
    val orderStatus: OrderStatus,
)