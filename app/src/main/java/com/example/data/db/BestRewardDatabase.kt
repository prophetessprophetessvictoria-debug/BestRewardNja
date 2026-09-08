package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppConfig
import com.example.data.model.OfferItem
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.UserCompletedOffer
import com.example.data.model.WithdrawalRequest

@Database(
    entities = [
        UserAccount::class,
        TransactionRecord::class,
        WithdrawalRequest::class,
        OfferItem::class,
        UserCompletedOffer::class,
        AppConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BestRewardDatabase : RoomDatabase() {
    abstract fun dao(): BestRewardDao

    companion object {
        @Volatile
        private var INSTANCE: BestRewardDatabase? = null

        fun getInstance(context: Context): BestRewardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BestRewardDatabase::class.java,
                    "bestrewardnja_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
