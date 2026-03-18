package com.fastcampus.deliveryapi.service.menu

import com.fastcampus.deliveryapi.repository.menu.Menu
import com.fastcampus.deliveryapi.repository.menu.MenuRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class MenuService (
    private val menuRepository : MenuRepository
){

    fun findAllByStoreId(storeId : Long) : List<Menu> {
        return menuRepository.findAllByStoreId(storeId)
    }
}