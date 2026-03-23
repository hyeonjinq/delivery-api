package com.fastcampus.deliveryapi.service.cart

import com.fastcampus.deliveryapi.exception.NotFoundException
import com.fastcampus.deliveryapi.repository.cart.Cart
import com.fastcampus.deliveryapi.repository.cart.CartRepository
import org.springframework.stereotype.Service

@Service
class CartService (
    private val cartRepository: CartRepository
){
    companion object {
        private const val INIT_QUANTITY = 1
    }

    fun findByCustomerId(customerId: Long) : Cart{
        val cartOptional = cartRepository.findAllByCustomerIdAndIsDeleted(customerId, false)
        if (cartOptional.isEmpty) {
            throw NotFoundException("고객님의 장바구니 정보를 찾을 수 없습니다.")
        }
        return cartOptional.get()
    }
}