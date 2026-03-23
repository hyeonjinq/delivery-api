package com.fastcampus.deliveryapi.controller.cart

import com.fastcampus.deliveryapi.controller.cart.dto.CartMenuDTO
import com.fastcampus.deliveryapi.controller.cart.dto.CartQueryRequest
import com.fastcampus.deliveryapi.controller.cart.dto.CartQueryResponse
import com.fastcampus.deliveryapi.service.cart.CartService
import com.fastcampus.deliveryapi.service.cartitem.CartItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CartController (
    private val cartService: CartService,
    private val cartItemService: CartItemService
){

    @GetMapping("/apis/carts/items")
    fun list(
        cartQueryRequest: CartQueryRequest
    ): ResponseEntity<CartQueryResponse> {
        val cart = cartService.findByCustomerId(cartQueryRequest.customerId);
        val cartMenus = cartItemService.findAllByCartId(cart.cartId)

        val cartMenuDTOs = cartMenus.map { CartMenuDTO.from(it) }
        return ResponseEntity.ok(CartQueryResponse(
            customerId = cart.customerId,
            cartItems = cartMenuDTOs
        ))
    }
}