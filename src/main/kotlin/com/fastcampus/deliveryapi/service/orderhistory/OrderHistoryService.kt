package com.fastcampus.deliveryapi.service.orderhistory

import com.fastcampus.deliveryapi.domain.order.OrderHistory
import com.fastcampus.deliveryapi.domain.order.OrderStatus
import com.fastcampus.deliveryapi.repository.order.OrderRepository
import com.fastcampus.deliveryapi.repository.orderitem.OrderItemRepository
import org.springframework.stereotype.Service

@Service
class OrderHistoryService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
) {

    fun findAll(customerId: Long, orderStatus: OrderStatus): List<OrderHistory> {
        val orderStores = orderRepository.findAllByCustomerId(customerId, orderStatus)
        return orderStores.map { orderStore ->
            val orderItemMenus = orderItemRepository.findAllByOrderId(orderStore.orderId);
            OrderHistory(
                orderId = orderStore.orderId,
                storeId = orderStore.storeId,
                storeName = orderStore.storeName,
                menuCount = orderItemMenus.size,
                menuNames = orderItemMenus.map { it.menuName },
                menuRepresentativeImageUrl = orderItemMenus.first().menuMainImageUrl,
                totalOrderAmount = orderStore.orderTotalAmount,
                orderStatus = orderStore.orderStatus,
            )
        }
    }
}