package com.fastcampus.deliveryapi.service.store

import com.fastcampus.deliveryapi.controller.catalog.category.dto.CategoryStoreDTO
import com.fastcampus.deliveryapi.domain.store.CategoryStore
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

    fun findByCategoryId(categoryId : Long) : List<CategoryStore> {
        return storeRepository.findAllByCategoryId(categoryId);
    }

    fun findByCategoryIdAndReviewGrade(categoryId: Long, reviewGradeFilterValue: Int): List<CategoryStore> {
        return storeRepository.findAllByCategoryIdAndReviewGrade(categoryId, reviewGradeFilterValue)
    }
}