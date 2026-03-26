package com.fastcampus.deliveryapi.controller.order

import com.fastcampus.deliveryapi.controller.order.dto.OrderDetailDTO
import com.fastcampus.deliveryapi.controller.order.dto.OrderDetailResponse
import com.fastcampus.deliveryapi.controller.order.dto.OrderRequest
import com.fastcampus.deliveryapi.controller.order.dto.OrderResponse
import com.fastcampus.deliveryapi.exception.NotFoundOrderException
import com.fastcampus.deliveryapi.service.order.OrderService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "OrderController", description = "주문 정보 컨트롤러")
@RestController
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping("/apis/orders")
    fun order(@RequestBody orderRequest: OrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.order(orderRequest)
        return ResponseEntity.ok(OrderResponse(order.orderId))
    }

    @GetMapping("/orders/{orderId}")
    fun detail(@PathVariable orderId: Long, @RequestParam customerId: Long): ResponseEntity<OrderDetailResponse> {
        val orderDetail = orderService.detail(orderId = orderId, customerId = customerId)
        if (orderDetail.customerId != customerId) {
            throw NotFoundOrderException("고객의 주문 정보를 찾을 수 없습니다. $customerId")
        }
        val orderDetailDTO = OrderDetailDTO.from(orderDetail)
        return ResponseEntity.ok(OrderDetailResponse(orderDetailDTO = orderDetailDTO))
    }

}