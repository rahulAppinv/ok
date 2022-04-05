package com.app.okra.data.db

import androidx.room.TypeConverter
import com.app.okra.data.db.table.SearchHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class Converters {

    @TypeConverter
    fun toString(arr: List<String>?): String? {
        var str = ""
        if (arr != null) {
            for (a in arr) {
                str +=  if(str.isEmpty())
                    a
                else
                    ",$a"

            }
        }
        return str
    }

    @TypeConverter
    fun toArray(str: String?):List<String?>? {
        return if (str != null){
            if(str.contains(",")){
                str.split(",".toRegex()).toList()
            }else{
                arrayOf(str).toList()
            }
        } else null
    }

    @TypeConverter
    fun toSearchHistoryListToString(arr: List<SearchHistory>?): String? {
        return  Gson().toJson(arr)
    }

    @TypeConverter
    fun toSearchHistoryArray(str: String?):List<SearchHistory?>? {
        val listType: Type = object : TypeToken<List<SearchHistory>?>() {}.type
        return  Gson().fromJson(str, listType)
    }

    @TypeConverter
    fun toSearchHistoryString(arr: SearchHistory): String? {
        return  Gson().toJson(arr)
    }

    @TypeConverter
    fun toSearchHistory(str: String?):SearchHistory? {
        return  Gson().fromJson(str, SearchHistory::class.java)
    }
}