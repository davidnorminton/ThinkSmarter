package com.example.thinksmarter.data.db

import androidx.room.*
import com.example.thinksmarter.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isDefault = 0 ORDER BY name ASC")
    fun getCustomCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE isDefault = 0")
    suspend fun deleteAllCustomCategories()

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
} 