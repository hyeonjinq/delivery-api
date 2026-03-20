package com.fastcampus.deliveryapi.controller.display.sdp

import com.fastcampus.deliveryapi.controller.catalog.meno.dto.MenuDTO
import com.fastcampus.deliveryapi.controller.display.sdp.dto.StoreDetailPageResponse
import com.fastcampus.deliveryapi.exception.NotFoundStoreException
import com.fastcampus.deliveryapi.service.menu.MenuService
import com.fastcampus.deliveryapi.service.store.StoreService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name = "StoreDetailPageController", description = "상점 상세 페이지 컨트롤러")
@RestController
class StoreDetailPageController (
    private val storeService: StoreService,
    private val menuService: MenuService
){
    private val logger = KotlinLogging.logger {}

    @GetMapping("/apis/display/stores/{storeId}")
    fun list(@PathVariable("storeId") storeId: Long): StoreDetailPageResponse {
        val storeOptional = storeService.findByStoreId(storeId);

        if(storeOptional.isEmpty) {
            throw NotFoundStoreException("상점 정보를 찾을 수 없습니다. $storeId")
        }
        val store = storeOptional.get();

        val menus = menuService.findAllByStoreId(storeId);
        val menuDTOs = menus.map { MenuDTO.from(it) }

        return StoreDetailPageResponse(
            storeId = store.storeId,
            storeName = store.storeName,
            phone = store.storePhone,
            address = store.address,
            /*deliveryFee = store.deliveryFee,
            deliveryTime = store.deliveryTime,
            minimumOrderPrice = store.minimumOrderPrice,
            reviewGrade = store.reviewGrade,
            storeMainImageUrl = store.storeMainImageUrl,
            description = store.description,*/
            menus = menuDTOs,
        )
    }
}