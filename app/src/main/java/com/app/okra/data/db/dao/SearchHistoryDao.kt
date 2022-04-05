package com.app.okra.data.db.dao

import androidx.annotation.NonNull
import androidx.room.*
import com.app.okra.data.db.table.SearchHistory
import retrofit2.http.DELETE

@Dao
interface  SearchHistoryDao {

    @Query("Select * from searchHistory")
    fun getAllData(): List<SearchHistory>

    @Query("Select * from searchHistory Where type = :type")
    fun getSearchHistoryData(type: String): List<SearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchHistory(searchHistory: SearchHistory)

    @Query("UPDATE searchHistory SET data = :searchHistory WHERE type = :type")
    fun updateSearchHistory(type :String, searchHistory: SearchHistory)

    @Query("DELETE FROM searchHistory")
    fun deleteSearchHistory()
}