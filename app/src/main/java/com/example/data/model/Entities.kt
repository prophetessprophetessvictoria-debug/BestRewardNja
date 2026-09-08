package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val coinsBalance: Long,
    val totalEarnedCoins: Long,
    val totalWithdrawnNaira: Double,
    val referralCode: String,
    val referredBy: String? = null,
    val lastDailyBonusDate: String = "",
    val dailyBonusStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val type: String, // REWARDED_AD, APP_OFFER, TASK, REFERRAL, DAILY_BONUS, WITHDRAWAL_HOLD, WITHDRAWAL_REFUND, WITHDRAWAL_PAID
    val title: String,
    val amountCoins: Long, // positive for earn, negative for deduction/hold
    val equivalentNaira: Double,
    val status: String, // COMPLETED, PENDING, REVERSED
    val reference: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val amountCoins: Long,
    val amountNaira: Double,
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val payoutMethod: String, // "Bank Transfer", "OPay Wallet", "Palmpay Wallet"
    val status: String, // "PENDING", "APPROVED", "PAID", "REJECTED"
    val rejectionReason: String? = null,
    val paymentReference: String? = null,
    val paidAt: Long? = null,
    val requestedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offers")
data class OfferItem(
    @PrimaryKey val id: String,
    val provider: String, // e.g. "TapCore Offers", "IronTask Network", "AdGate Nigeria"
    val title: String,
    val description: String,
    val category: String, // "APP_INSTALL", "APP_TRY", "TASK", "SURVEY"
    val rewardCoins: Long,
    val isAvailable: Boolean = true,
    val estimatedMinutes: Int = 3,
    val instructions: String = "Complete the installation and explore for at least 60 seconds to claim coins."
)

@Entity(tableName = "user_completed_offers", primaryKeys = ["userId", "offerId"])
data class UserCompletedOffer(
    val userId: String,
    val offerId: String,
    val completedAt: Long = System.currentTimeMillis(),
    val payoutCoins: Long
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val coinsPerRewardedAd: Long = 25,
    val dailyBonusBaseCoins: Long = 50,
    val referralBonusCoins: Long = 100,
    val coinsToNairaRate: Double = 1.0, // 1 Coin = ₦1.00 (e.g., 1000 coins = ₦1,000)
    val minWithdrawalNaira: Double = 1000.0, // ₦1,000 minimum withdrawal
    val adsEnabled: Boolean = true,
    val offersEnabled: Boolean = true,
    val tasksEnabled: Boolean = true,
    val referralsEnabled: Boolean = true,
    val dailyBonusEnabled: Boolean = true,
    val withdrawalsEnabled: Boolean = true,
    val estimatedAdRevenuePerAdNaira: Double = 1.50, // BestRewardNja ad network income per view
    val estimatedOfferMarginNaira: Double = 30.0, // Profit earned per completed partner task
    val adminPin: String = "1234"
)
