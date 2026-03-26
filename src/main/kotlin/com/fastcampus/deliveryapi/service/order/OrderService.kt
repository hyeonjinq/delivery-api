package com.fastcampus.deliveryapi.service.order

import com.fastcampus.deliveryapi.controller.order.dto.OrderRequest
import com.fastcampus.deliveryapi.domain.catalog.menu.MenuStatus
import com.fastcampus.deliveryapi.domain.order.OrderDetail
import com.fastcampus.deliveryapi.domain.order.OrderUUIDGenerator
import com.fastcampus.deliveryapi.domain.store.StoreStatus
import com.fastcampus.deliveryapi.exception.DuplicateOrderException
import com.fastcampus.deliveryapi.exception.InvalidOrderException
import com.fastcampus.deliveryapi.exception.InvalidQuantityException
import com.fastcampus.deliveryapi.exception.MenuNotAvailableException
import com.fastcampus.deliveryapi.exception.NotFoundCheckoutException
import com.fastcampus.deliveryapi.exception.NotFoundMenuException
import com.fastcampus.deliveryapi.exception.NotFoundOrderException
import com.fastcampus.deliveryapi.exception.NotFoundStoreException
import com.fastcampus.deliveryapi.exception.StoreNotAvailableException
import com.fastcampus.deliveryapi.repository.checkout.Checkout
import com.fastcampus.deliveryapi.repository.checkout.CheckoutRepository
import com.fastcampus.deliveryapi.repository.checkoutitem.CheckoutItem
import com.fastcampus.deliveryapi.repository.checkoutitem.CheckoutItemRepository
import com.fastcampus.deliveryapi.repository.menu.MenuRepository
import com.fastcampus.deliveryapi.repository.order.Order
import com.fastcampus.deliveryapi.repository.order.OrderRepository
import com.fastcampus.deliveryapi.repository.orderdiscount.OrderDiscountItem
import com.fastcampus.deliveryapi.repository.orderdiscount.OrderDiscountItemRepository
import com.fastcampus.deliveryapi.repository.orderitem.OrderItem
import com.fastcampus.deliveryapi.repository.orderitem.OrderItemRepository
import com.fastcampus.deliveryapi.repository.store.StoreRepository
import com.fastcampus.deliveryapi.service.cart.CartItemService
import com.fastcampus.deliveryapi.service.cart.CartService
import com.fastcampus.deliveryapi.service.discount.DiscountService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class OrderService(
    private val cartService: CartService,
    private val cartItemService: CartItemService,
    private val discountService: DiscountService,
    private val checkoutRepository: CheckoutRepository,
    private val checkoutItemRepository: CheckoutItemRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderDiscountItemRepository : OrderDiscountItemRepository,
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository
) {

    @Value("\${server.role-name}")
    lateinit var roleName: String

    @Value("\${order.checkout-expiry-minutes:30}")
    private var checkoutExpiryMinutes: Long = 30

    fun order(orderRequest: OrderRequest): Order {
        // 1. 체크아웃 존재 여부 및 기본 검증
        val checkout = validateAndGetCheckout(orderRequest.checkoutId)

        // 2. 중복 주문 방지
        validateDuplicatedOrder(checkout.checkoutId)

        // 3. 가게 유효성 검증
        val store = validateStore(checkout.storeId)

        // 4. 체크아웃 아이템 조회
        val checkoutItems = checkoutItemRepository.findAllByCheckoutId(checkout.checkoutId)
        validateCheckoutItemsNotEmpty(checkoutItems)

        // 5. 체크아웃 아이템들의 메뉴 유효성 검증
        validateCheckoutItems(checkoutItems, store.storeId)

        // 6. 주문 금액 계산
        val orderAmount = calculateOrderAmount(checkoutItems)
        val maxDiscount = discountService.findDiscountBy(checkoutId = checkout.checkoutId)
        val discountValue = maxDiscount?.discountValue?.let { BigDecimal(it) } ?: BigDecimal(0)
        val totalAmount = orderAmount.minus(discountValue)

        // 7. 최소 주문 금액 검증
        validateMinimumOrderAmount(totalAmount)

        // 8. 주문 생성
        val createdOrder = createOrder(checkout, orderAmount, discountValue, totalAmount)

        // 9. 주문 아이템 생성
        val orderItems = createOrderItems(checkoutItems, createdOrder)

        // 10. 장바구니 아이템 삭제
        removeCartItems(orderRequest, orderItems)

        return createdOrder
    }

    private fun createOrder(
        checkout: Checkout,
        orderAmount: BigDecimal,
        discountValue: BigDecimal,
        totalAmount: BigDecimal
    ): Order {
        val orderUUID = OrderUUIDGenerator.gen()
        val order = Order(
            orderUUID = orderUUID.id,
            orderShortenId = orderUUID.shortenId,
            checkoutId = checkout.checkoutId,
            orderAmount = orderAmount,
            discountAmount = discountValue,
            deliveryFee = BigDecimal.ZERO,
            totalAmount = totalAmount,
            storeId = checkout.storeId,
            customerId = checkout.customerId,
        )
        order.createdBy = roleName
        order.updatedBy = roleName
        val createdOrder = orderRepository.save(order)
        return createdOrder
    }

    private fun createOrderItems(
        checkoutItems: List<CheckoutItem>,
        createdOrder: Order
    ): List<OrderItem> {
        val orderItems = checkoutItems.map {
            val orderItem = OrderItem(
                orderId = createdOrder.orderId,
                menuId = it.menuId,
                menuPrice = it.menuPrice,
                menuQuantity = it.menuQuantity,
            )
            orderItem.createdBy = roleName
            orderItem.updatedBy = roleName
            orderItem
        }
        orderItemRepository.saveAll(orderItems)
        return orderItems
    }

    private fun removeCartItems(
        orderRequest: OrderRequest,
        orderItems: List<OrderItem>
    ) {
        val cart = cartService.findByCustomerId(customerId = orderRequest.customerId)
        val orderedMenuIds = orderItems.map { it.menuId }.toList()
        cartItemService.remove(cartId = cart.cartId, orderedMenuIds = orderedMenuIds)
    }

    private fun validateDuplicatedOrder(checkoutId: Long) {
        val existsByCheckoutId = orderRepository.existsByCheckoutId(checkoutId)
        if (existsByCheckoutId) {
            throw DuplicateOrderException("이미 처리된 주문입니다. checkoutId: $checkoutId")
        }
    }

    /**
     * 1. 체크아웃 존재 여부 검증
     */
    private fun validateAndGetCheckout(checkoutId: Long): Checkout {
        val checkoutOptional = checkoutRepository.findById(checkoutId)
        if (checkoutOptional.isEmpty) {
            throw NotFoundCheckoutException("체크아웃 정보를 찾을 수 없습니다. checkoutId: $checkoutId")
        }
        val checkout = checkoutOptional.get()

        // 체크아웃 만료 시간 검증 (30분 이상 된 체크아웃은 거절)
        validateCheckoutExpiry(checkout)

        return checkout
    }

    /**
     * 2. 체크아웃 만료 시간 검증
     */
    private fun validateCheckoutExpiry(checkout: Checkout) {
        val createdAt = checkout.createdAt ?: OffsetDateTime.now()
        val expiryTime = createdAt.plusMinutes(checkoutExpiryMinutes)
        if (OffsetDateTime.now().isAfter(expiryTime)) {
            throw InvalidOrderException("체크아웃이 만료되었습니다. 새로 장바구니에서 주문해주세요.")
        }
    }

    /**
     * 3. 가게 유효성 검증
     */
    private fun validateStore(storeId: Long): com.fastcampus.deliveryapi.repository.store.Store {
        val storeOptional = storeRepository.findById(storeId)
        if (storeOptional.isEmpty) {
            throw NotFoundStoreException("가게 정보를 찾을 수 없습니다. storeId: $storeId")
        }
        val store = storeOptional.get()

        // 가게 운영 상태 확인 (판매 중 상태만 주문 가능)
        if (store.storeStatus != StoreStatus.SALE) {
            throw StoreNotAvailableException("현재 주문을 받을 수 없는 가게입니다. 상태: ${store.storeStatus}")
        }

        return store
    }

    /**
     * 4. 체크아웃 아이템 존재 여부 검증
     */
    private fun validateCheckoutItemsNotEmpty(checkoutItems: List<CheckoutItem>) {
        if (checkoutItems.isEmpty()) {
            throw InvalidOrderException("주문할 상품이 없습니다.")
        }
    }

    /**
     * 5. 체크아웃 아이템들의 메뉴 유효성 검증
     */
    private fun validateCheckoutItems(checkoutItems: List<CheckoutItem>, storeId: Long) {
        for (item in checkoutItems) {
            // 메뉴 수량 유효성 검증
            validateMenuQuantity(item.menuQuantity)

            // 메뉴 존재 여부 검증
            val menuOptional = menuRepository.findById(item.menuId)
            if (menuOptional.isEmpty) {
                throw NotFoundMenuException("메뉴를 찾을 수 없습니다. menuId: ${item.menuId}")
            }
            val menu = menuOptional.get()

            // 메뉴가 요청한 가게에 속하는지 확인
            if (menu.storeId != storeId) {
                throw InvalidOrderException("요청한 가게에 속하지 않은 메뉴가 포함되어 있습니다. menuId: ${item.menuId}")
            }

            // 메뉴 상태 확인 (판매 중 상태만 주문 가능)
            validateMenuStatus(menu.menuStatus, item.menuId)

            // 메뉴가 삭제되지 않았는지 확인
            if (menu.isDeleted) {
                throw MenuNotAvailableException("삭제된 메뉴는 주문할 수 없습니다. menuId: ${item.menuId}")
            }

            // 메뉴 가격과 체크아웃 가격 일치 검증
            validateMenuPrice(menu.price, item.menuPrice, item.menuId)
        }
    }

    /**
     * 메뉴 수량 유효성 검증
     */
    private fun validateMenuQuantity(quantity: Int) {
        if (quantity <= 0) {
            throw InvalidQuantityException("메뉴 수량은 1 이상이어야 합니다. quantity: $quantity")
        }
        // 합리적 범위 검증 (한 번에 최대 100개까지만 주문 가능)
        if (quantity > 100) {
            throw InvalidQuantityException("메뉴 수량이 너무 많습니다. 최대 100개까지 주문 가능합니다.")
        }
    }

    /**
     * 메뉴 상태 검증
     */
    private fun validateMenuStatus(menuStatus: MenuStatus, menuId: Long) {
        // SALE 상태만 주문 가능
        if (menuStatus != MenuStatus.SALE) {
            throw MenuNotAvailableException("현재 주문할 수 없는 메뉴입니다. 상태: $menuStatus, menuId: $menuId")
        }
    }

    /**
     * 메뉴 가격 일치 검증 (가격 조작 방지)
     */
    private fun validateMenuPrice(actualPrice: BigDecimal, checkoutPrice: BigDecimal, menuId: Long) {
        if (actualPrice != checkoutPrice) {
            throw InvalidOrderException(
                "메뉴 가격이 변경되었습니다. 다시 장바구니에서 확인 후 주문해주세요. " +
                "menuId: $menuId, 현재 가격: $actualPrice, 기존 가격: $checkoutPrice"
            )
        }
    }

    /**
     * 주문 금액 계산
     */
    private fun calculateOrderAmount(checkoutItems: List<CheckoutItem>): BigDecimal {
        val menuPrices = checkoutItems.map { it.menuPrice.multiply(BigDecimal(it.menuQuantity)) }
        return menuPrices.sumOf { it }
    }

    /**
     * 최소 주문 금액 검증
     */
    private fun validateMinimumOrderAmount(totalAmount: BigDecimal) {
        // 최소 주문 금액을 1,000원으로 설정 (필요시 설정값으로 변경 가능)
        val minimumOrderAmount = BigDecimal(1000)
        if (totalAmount < minimumOrderAmount) {
            throw InvalidOrderException("최소 주문 금액은 ${minimumOrderAmount}원입니다. 현재 금액: ${totalAmount}원")
        }
    }

    /**
     * 주문 상세 조회
     */
    fun detail(orderId: Long): OrderDetail {
        val orderOptional = orderRepository.findById(orderId)
        if (orderOptional.isEmpty) {
            throw NotFoundOrderException("요청한 주문서($orderId) 정보를 찾을 수 없습니다.")
        }

        val order = orderOptional.get()
        val orderItemMenus = orderItemRepository.findAllByOrderId(orderId = orderId)
        val orderDiscountItems = orderDiscountItemRepository.findAllByOrderId(orderId = orderId)
        val orderDiscountItem: OrderDiscountItem? = if (orderDiscountItems.isNotEmpty()) orderDiscountItems.first() else null

        return OrderDetail(
            orderId = orderId,
            customerId = order.customerId,
            storeId = order.storeId,
            orderItems = orderItemMenus,
            orderDiscountItem = orderDiscountItem
        )
    }
}