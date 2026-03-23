package com.fastcampus.deliveryapi.controller.checkout

import com.fastcampus.deliveryapi.controller.checkout.dto.CheckoutRequest
import com.fastcampus.deliveryapi.controller.checkout.dto.CheckoutResponse
import com.fastcampus.deliveryapi.service.cart.CartService
import com.fastcampus.deliveryapi.service.cart.CartItemService
import com.fastcampus.deliveryapi.service.checkout.CheckoutService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@Tag(name = "주문을 위한 체크아웃 API", description = "결제를 위한 체크아웃 API")
@RestController
class CheckoutController (
    private val cartService: CartService,
    private val cartItemService: CartItemService,
    private val checkoutService: CheckoutService,
){

    @PostMapping("/apis/checkouts")
    fun createCheckout(
        @RequestBody checkoutRequest: CheckoutRequest
    ): ResponseEntity<CheckoutResponse> {
        val cart = cartService.findByCustomerId(checkoutRequest.customerId)
        val cartMenus = cartItemService.findAllByCartId(cart.cartId)
        val checkout = checkoutService.create(checkoutRequest.customerId, checkoutRequest.discountId, cartMenus)
        return ResponseEntity.ok(
            CheckoutResponse(
                customerId = checkoutRequest.customerId,
                checkoutId = checkout.checkoutId,
            )
        )
    }
}