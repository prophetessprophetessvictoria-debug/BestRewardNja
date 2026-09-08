package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppConfig
import com.example.data.model.OfferItem
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.UserCompletedOffer
import com.example.data.model.WithdrawalRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface BestRewardDao {

    // User Operations
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUser(userId: String): Flow<UserAccount?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserOnce(userId: String): UserAccount?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE users SET coinsBalance = :newBalance WHERE userId = :userId")
    suspend fun updateBalance(userId: String, newBalance: Long)

    // Transaction Operations
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(record: TransactionRecord): Long

    // Withdrawal Operations
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalRequest>>

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalRequest>>

    @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalRequest?

    @Query("SELECT * FROM withdrawals WHERE userId = :userId")
    suspend fun getPastWithdrawalsForUser(userId: String): List<WithdrawalRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalRequest): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalRequest)

    // Offer Operations
    @Query("SELECT * FROM offers ORDER BY rewardCoins DESC")
    fun getAllOffers(): Flow<List<OfferItem>>

    @Query("SELECT * FROM offers WHERE id = :id LIMIT 1")
    suspend fun getOfferById(id: String): OfferItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferItem>)

    @Update
    suspend fun updateOffer(offer: OfferItem)

    // Completed Offer Operations (Prevent duplicate exploits)
    @Query("SELECT * FROM user_completed_offers WHERE userId = :userId")
    fun getCompletedOffersForUser(userId: String): Flow<List<UserCompletedOffer>>

    @Query("SELECT COUNT(*) FROM user_completed_offers WHERE userId = :userId AND offerId = :offerId")
    suspend fun hasCompletedOffer(userId: String, offerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordOfferCompletion(completion: UserCompletedOffer)

    // Config Operations
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigOnce(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AppConfig)
}
