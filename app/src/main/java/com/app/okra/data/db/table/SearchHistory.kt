package com.app.okra.data.db.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searchHistory")
data class SearchHistory (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id :Int=0,
    @ColumnInfo(name = "type")
    val type : String? =null,
    @ColumnInfo(name = "data")
    val data : List<String>?=null,

)
