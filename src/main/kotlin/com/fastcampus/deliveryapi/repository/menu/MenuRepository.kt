package com.fastcampus.deliveryapi.repository.menu

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MenuRepository : JpaRepository<Menu, Long> {
    fun findAllByStoreId(storeId : Long) : List<Menu>

    fun findByMenuIdAndStoreId(menuId : Long, storeId : Long) : Optional<Menu>
}