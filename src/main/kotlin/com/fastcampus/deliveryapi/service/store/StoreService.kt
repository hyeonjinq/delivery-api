package com.fastcampus.deliveryapi.service.store

import com.fastcampus.deliveryapi.repository.store.Store
import com.fastcampus.deliveryapi.repository.store.StoreRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.Optional

@Service
@Transactional
class StoreService (
    private val storeRepository : StoreRepository
){

    fun findByStoreId(storeId : Long) : Optional<Store> {
        return storeRepository.findById(storeId);
    }
}