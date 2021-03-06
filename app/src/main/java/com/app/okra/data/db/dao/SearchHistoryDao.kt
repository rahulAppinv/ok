package com.app.okra.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.okra.data.db.table.SearchHistory

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