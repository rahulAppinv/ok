package com.app.okra.data.db

import android.content.Context
import androidx.room.*
import com.app.okra.data.db.dao.SearchHistoryDao
import com.app.okra.data.db.table.SearchHistory

@Database(
    entities = [SearchHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {

    abstract fun searchHistoryDao() : SearchHistoryDao

    private fun AppDb() {}



    companion object {
        @Volatile private var instance: AppDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppDb::class.java, "okra.db")
            .build()



       /* private var instance: AppDb? = null

        fun getAppDataBase(context: Context): AppDb? {
            if (instance == null){
                synchronized(AppDb::class){
                    instance = Room.databaseBuilder(
                        context, AppDb::class.java, "GroupaDB")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }

        fun destroyDataBase(){
            instance = null
        }*/
    }



}