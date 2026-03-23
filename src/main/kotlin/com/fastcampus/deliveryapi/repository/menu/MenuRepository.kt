package com.fastcampus.deliveryapi.repository.menu

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MenuRepository : JpaRepository<Menu, Long> {
    fun findAllByStoreId(storeId : Long) : List<Menu>

    fun findByMenuIdAndStoreId(menuId : Long, storeId : Long) : Optional<Menu>
}