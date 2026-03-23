package com.fastcampus.deliveryapi.controller.cart.dto

import com.fastcampus.deliveryapi.domain.cart.CartMenu
import java.math.BigDecimal

data class CartMenuDTO(
    val cartItemId: Long,
    val menuId: Long,
    val menuName: String,
    val menuImageUrl: String,
    val quantity: Int,
    val totalPrice: BigDecimal,
) {
    companion object {
        fun from(cartMenu: CartMenu): CartMenuDTO {
            return CartMenuDTO(
                cartItemId = cartMenu.cartItemId,
                menuId = cartMenu.menuId,
                menuName = cartMenu.menuName,
                menuImageUrl = cartMenu.menuImageUrl,
                quantity = cartMenu.quantity,
                totalPrice = cartMenu.price.multiply(BigDecimal(cartMenu.quantity))
            )
        }
    }
}
