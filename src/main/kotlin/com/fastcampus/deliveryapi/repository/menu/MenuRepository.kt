package com.fastcampus.deliveryapi.repository.menu

import org.springframework.data.jpa.repository.JpaRepository

interface MenuRepository : JpaRepository<Menu, Long> {
    fun findAllByStoreId(storeId : Long) : List<Menu>
}