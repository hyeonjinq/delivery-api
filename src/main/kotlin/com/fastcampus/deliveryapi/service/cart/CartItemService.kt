package com.fastcampus.deliveryapi.service.cart

import com.fastcampus.deliveryapi.domain.cart.CartMenu
import com.fastcampus.deliveryapi.repository.cartitem.CartItemRepository
import org.springframework.stereotype.Service

@Service
class CartItemService(
private val cartItemRepository: CartItemRepository
) {
    fun findAllByCartId(cartId : Long) : List<CartMenu> {
        return cartItemRepository.findAllByCartId(cartId)
    }
}