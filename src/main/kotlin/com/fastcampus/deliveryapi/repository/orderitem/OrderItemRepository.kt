package com.fastcampus.deliveryapi.repository.orderitem

import com.fastcampus.deliveryapi.domain.orderitem.OrderItemMenu
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderItemRepository: JpaRepository<OrderItem, Long> {
    @Query(
        value = """
            SELECT new com.fastcampus.deliveryapi.domain.orderitem.OrderItemMenu(
                oi.orderId
                , oi.orderItemId
                , oi.menuId
                , m.menuName
                , oi.menuPrice * oi.menuQuantity
                , m.menuMainImageUrl
            )  
            FROM 
                OrderItem oi
                JOIN Menu m 
                ON m.menuId = oi.menuId
            WHERE oi.orderId = :orderId
        """,
    )
    fun findAllByOrderId(@Param("orderId") orderId: Long): List<OrderItemMenu>
}