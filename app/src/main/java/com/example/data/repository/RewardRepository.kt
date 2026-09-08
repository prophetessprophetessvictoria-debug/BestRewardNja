package com.example.data.repository

import android.content.Context
import com.example.data.db.BestRewardDao
import com.example.data.db.BestRewardDatabase
import com.example.data.model.AppConfig
import com.example.data.model.OfferItem
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.UserCompletedOffer
import com.example.data.model.WithdrawalRequest
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ProfitabilityStats(
    val totalCoinsIssued: Long,
    val totalWithdrawalsPendingNaira: Double,
    val totalWithdrawalsPaidNaira: Double,
    val estimatedAdRevenueNaira: Double,
    val estimatedOfferRevenueNaira: Double,
    val totalEstimatedRevenueNaira: Double,
    val totalPayoutLiabilityNaira: Double,
    val netEstimatedProfitNaira: Double,
    val isProfitable: Boolean
)

data class UserVerificationSummary(
    val user: UserAccount,
    val currentBalance: Long,
    val previousWithdrawalsCount: Int,
    val previousPaidTotalNaira: Double,
    val pastRequests: List<WithdrawalRequest>
)

class RewardRepository(private val dao: BestRewardDao) {

    companion object {
        private var INSTANCE: RewardRepository? = null

        fun getInstance(context: Context): RewardRepository {
            return INSTANCE ?: synchronized(this) {
                val db = BestRewardDatabase.getInstance(context)
                val repo = RewardRepository(db.dao())
                INSTANCE = repo
                repo
            }
        }
    }

    val allUsers: Flow<List<UserAccount>> = dao.getAllUsers()
    val allTransactions: Flow<List<TransactionRecord>> = dao.getAllTransactions()
    val allWithdrawals: Flow<List<WithdrawalRequest>> = dao.getAllWithdrawals()
    val allOffers: Flow<List<OfferItem>> = dao.getAllOffers()
    val appConfig: Flow<AppConfig?> = dao.getConfig()

    fun getUser(userId: String): Flow<UserAccount?> = dao.getUser(userId)
    fun getTransactionsForUser(userId: String): Flow<List<TransactionRecord>> = dao.getTransactionsForUser(userId)
    fun getWithdrawalsForUser(userId: String): Flow<List<WithdrawalRequest>> = dao.getWithdrawalsForUser(userId)
    fun getCompletedOffersForUser(userId: String): Flow<List<UserCompletedOffer>> = dao.getCompletedOffersForUser(userId)

    suspend fun initializeDefaultsIfNeeded() {
        // Init default AppConfig if absent
        val existingConfig = dao.getConfigOnce()
        if (existingConfig == null) {
            dao.saveConfig(
                AppConfig(
                    id = 1,
                    coinsPerRewardedAd = 25,
                    dailyBonusBaseCoins = 50,
                    referralBonusCoins = 100,
                    coinsToNairaRate = 1.0, // 1 coin = ₦1.00
                    minWithdrawalNaira = 1000.0,
                    adsEnabled = true,
                    offersEnabled = true,
                    tasksEnabled = true,
                    referralsEnabled = true,
                    dailyBonusEnabled = true,
                    withdrawalsEnabled = true,
                    estimatedAdRevenuePerAdNaira = 2.0,
                    estimatedOfferMarginNaira = 45.0,
                    adminPin = "1234"
                )
            )
        }

        // Init default primary demo user
        val existingUser = dao.getUserOnce("USR_001")
        if (existingUser == null) {
            val defaultUser = UserAccount(
                userId = "USR_001",
                name = "Chidi Eze",
                email = "chidi.eze@gmail.com",
                phone = "08031234567",
                coinsBalance = 1250,
                totalEarnedCoins = 1450,
                totalWithdrawnNaira = 0.0,
                referralCode = "BRN-CHIDI99",
                referredBy = null,
                lastDailyBonusDate = "",
                dailyBonusStreak = 1
            )
            dao.insertUser(defaultUser)

            // Seed welcome transaction
            dao.insertTransaction(
                TransactionRecord(
                    userId = "USR_001",
                    type = "WELCOME_BONUS",
                    title = "Welcome Registration Bonus",
                    amountCoins = 200,
                    equivalentNaira = 200.0,
                    status = "COMPLETED",
                    reference = "REG-" + UUID.randomUUID().toString().take(8).uppercase()
                )
            )
            dao.insertTransaction(
                TransactionRecord(
                    userId = "USR_001",
                    type = "REWARDED_AD",
                    title = "Rewarded Video Ad Completion",
                    amountCoins = 50,
                    equivalentNaira = 50.0,
                    status = "COMPLETED",
                    reference = "AD-" + UUID.randomUUID().toString().take(8).uppercase()
                )
            )
            dao.insertTransaction(
                TransactionRecord(
                    userId = "USR_001",
                    type = "APP_OFFER",
                    title = "Installed & Explored OPay App",
                    amountCoins = 1000,
                    equivalentNaira = 1000.0,
                    status = "COMPLETED",
                    reference = "OFFER-OPAY-001"
                )
            )
        }

        // Init legitimate partner offers catalogue
        val initialOffers = listOf(
            OfferItem(
                id = "OFFER_OPAY_01",
                provider = "TapCore Offers",
                title = "Try OPay SuperApp",
                description = "Install the official OPay app and explore features for at least 60 seconds.",
                category = "APP_TRY",
                rewardCoins = 300,
                isAvailable = true,
                estimatedMinutes = 2,
                instructions = "1. Download via the verified store.\n2. Open and browse transfer services for 60 seconds.\n3. Return to claim 300 coins."
            ),
            OfferItem(
                id = "OFFER_PIGGYVEST_02",
                provider = "IronTask Network",
                title = "PiggyVest Savings Tour",
                description = "Complete free registration on PiggyVest and create your first target saving plan.",
                category = "TASK",
                rewardCoins = 500,
                isAvailable = true,
                estimatedMinutes = 4,
                instructions = "1. Complete user profile.\n2. Verify email address.\n3. BestRewardNja will verify provider callback within 1 minute."
            ),
            OfferItem(
                id = "OFFER_KUDABANK_03",
                provider = "AdGate Nigeria",
                title = "Install Kuda Bank App",
                description = "Download Kuda Digital Bank and sign up for zero fee banking.",
                category = "APP_INSTALL",
                rewardCoins = 350,
                isAvailable = true,
                estimatedMinutes = 3,
                instructions = "Legitimate app installation verified directly by AdGate tracking SDK."
            ),
            OfferItem(
                id = "OFFER_FINTECH_SURVEY_04",
                provider = "OpinionPoint Africa",
                title = "2026 Nigerian Mobile Banking Survey",
                description = "Share your opinions on mobile banking speed and security in Nigeria.",
                category = "SURVEY",
                rewardCoins = 200,
                isAvailable = true,
                estimatedMinutes = 5,
                instructions = "Answer all 8 questionnaire prompts truthfully to qualify."
            )
        )
        dao.insertOffers(initialOffers)
    }

    suspend fun watchAdComplete(userId: String): Result<Long> {
        val config = dao.getConfigOnce() ?: AppConfig()
        if (!config.adsEnabled) {
            return Result.failure(Exception("Rewarded Ads are currently disabled by the administrator."))
        }
        val user = dao.getUserOnce(userId)
            ?: return Result.failure(Exception("User not found."))

        val rewardCoins = config.coinsPerRewardedAd
        val newBalance = user.coinsBalance + rewardCoins
        val newTotal = user.totalEarnedCoins + rewardCoins
        val nairaVal = rewardCoins * config.coinsToNairaRate

        dao.updateUser(user.copy(coinsBalance = newBalance, totalEarnedCoins = newTotal))

        val adRef = "AD-" + UUID.randomUUID().toString().take(8).uppercase()
        dao.insertTransaction(
            TransactionRecord(
                userId = userId,
                type = "REWARDED_AD",
                title = "Watched Rewarded Video Ad ($rewardCoins Coins)",
                amountCoins = rewardCoins,
                equivalentNaira = nairaVal,
                status = "COMPLETED",
                reference = adRef
            )
        )
        return Result.success(rewardCoins)
    }

    suspend fun claimDailyBonus(userId: String): Result<Long> {
        val config = dao.getConfigOnce() ?: AppConfig()
        if (!config.dailyBonusEnabled) {
            return Result.failure(Exception("Daily Bonus is currently disabled by administrator."))
        }
        val user = dao.getUserOnce(userId)
            ?: return Result.failure(Exception("User not found."))

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (user.lastDailyBonusDate == today) {
            return Result.failure(Exception("You have already claimed today's bonus! Check back tomorrow."))
        }

        val newStreak = user.dailyBonusStreak + 1
        val streakBonus = (newStreak - 1).coerceAtMost(7) * 10L
        val totalBonus = config.dailyBonusBaseCoins + streakBonus
        val newBalance = user.coinsBalance + totalBonus
        val newTotal = user.totalEarnedCoins + totalBonus

        dao.updateUser(
            user.copy(
                coinsBalance = newBalance,
                totalEarnedCoins = newTotal,
                lastDailyBonusDate = today,
                dailyBonusStreak = newStreak
            )
        )

        dao.insertTransaction(
            TransactionRecord(
                userId = userId,
                type = "DAILY_BONUS",
                title = "Day $newStreak Daily Bonus ($totalBonus Coins)",
                amountCoins = totalBonus,
                equivalentNaira = totalBonus * config.coinsToNairaRate,
                status = "COMPLETED",
                reference = "BONUS-$today"
            )
        )
        return Result.success(totalBonus)
    }

    suspend fun completeOffer(userId: String, offerId: String): Result<Long> {
        val config = dao.getConfigOnce() ?: AppConfig()
        if (!config.offersEnabled && !config.tasksEnabled) {
            return Result.failure(Exception("Partner offers and tasks are currently disabled by administrator."))
        }
        val user = dao.getUserOnce(userId)
            ?: return Result.failure(Exception("User not found."))

        // Check duplicate completion
        val alreadyCompleted = dao.hasCompletedOffer(userId, offerId) > 0
        if (alreadyCompleted) {
            return Result.failure(Exception("You have already completed this offer. One-time offers cannot be repeated."))
        }

        val offer = dao.getOfferById(offerId)
            ?: return Result.failure(Exception("Offer not found."))
        if (!offer.isAvailable) {
            return Result.failure(Exception("This offer is currently unavailable."))
        }

        val reward = offer.rewardCoins
        val newBalance = user.coinsBalance + reward
        val newTotal = user.totalEarnedCoins + reward

        dao.recordOfferCompletion(
            UserCompletedOffer(
                userId = userId,
                offerId = offerId,
                completedAt = System.currentTimeMillis(),
                payoutCoins = reward
            )
        )
        dao.updateUser(user.copy(coinsBalance = newBalance, totalEarnedCoins = newTotal))

        dao.insertTransaction(
            TransactionRecord(
                userId = userId,
                type = "APP_OFFER",
                title = "Completed: ${offer.title} (${offer.provider})",
                amountCoins = reward,
                equivalentNaira = reward * config.coinsToNairaRate,
                status = "COMPLETED",
                reference = "OFFER-${UUID.randomUUID().toString().take(8).uppercase()}"
            )
        )
        return Result.success(reward)
    }

    suspend fun applyReferralCode(userId: String, referralCode: String): Result<Long> {
        val config = dao.getConfigOnce() ?: AppConfig()
        if (!config.referralsEnabled) {
            return Result.failure(Exception("Referral program is currently disabled by administrator."))
        }
        val user = dao.getUserOnce(userId)
            ?: return Result.failure(Exception("User not found."))
        if (user.referredBy != null) {
            return Result.failure(Exception("You have already redeemed a referral invitation."))
        }
        if (referralCode.trim().equals(user.referralCode, ignoreCase = true)) {
            return Result.failure(Exception("You cannot use your own referral code."))
        }

        val bonus = config.referralBonusCoins
        val newBalance = user.coinsBalance + bonus
        val newTotal = user.totalEarnedCoins + bonus

        dao.updateUser(user.copy(coinsBalance = newBalance, totalEarnedCoins = newTotal, referredBy = referralCode))
        dao.insertTransaction(
            TransactionRecord(
                userId = userId,
                type = "REFERRAL",
                title = "Referral Bonus (Code: $referralCode)",
                amountCoins = bonus,
                equivalentNaira = bonus * config.coinsToNairaRate,
                status = "COMPLETED",
                reference = "REF-${UUID.randomUUID().toString().take(8).uppercase()}"
            )
        )
        return Result.success(bonus)
    }

    suspend fun requestWithdrawal(
        userId: String,
        amountNaira: Double,
        bankName: String,
        accountNumber: String,
        accountName: String,
        payoutMethod: String
    ): Result<WithdrawalRequest> {
        val config = dao.getConfigOnce() ?: AppConfig()
        if (!config.withdrawalsEnabled) {
            return Result.failure(Exception("Withdrawals are temporarily paused by administrator."))
        }
        if (amountNaira < config.minWithdrawalNaira) {
            return Result.failure(Exception("Minimum withdrawal amount is ₦${String.format(Locale.US, "%,.0f", config.minWithdrawalNaira)}."))
        }
        val user = dao.getUserOnce(userId)
            ?: return Result.failure(Exception("User not found."))

        val requiredCoins = (amountNaira / config.coinsToNairaRate).toLong()
        if (user.coinsBalance < requiredCoins) {
            return Result.failure(Exception("Insufficient coin balance. You need $requiredCoins coins for this withdrawal, but you have ${user.coinsBalance} coins."))
        }

        // Deduct coins atomically into hold
        val newBalance = user.coinsBalance - requiredCoins
        dao.updateUser(user.copy(coinsBalance = newBalance))

        val withdrawal = WithdrawalRequest(
            userId = userId,
            userName = user.name,
            amountCoins = requiredCoins,
            amountNaira = amountNaira,
            bankName = bankName,
            accountNumber = accountNumber,
            accountName = accountName,
            payoutMethod = payoutMethod,
            status = "PENDING",
            requestedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = dao.insertWithdrawal(withdrawal)
        val created = withdrawal.copy(id = id)

        // Record pending hold transaction
        dao.insertTransaction(
            TransactionRecord(
                userId = userId,
                type = "WITHDRAWAL_HOLD",
                title = "Withdrawal Request Pending (₦${String.format(Locale.US, "%,.0f", amountNaira)})",
                amountCoins = -requiredCoins,
                equivalentNaira = -amountNaira,
                status = "PENDING",
                reference = "WD-REQ-$id"
            )
        )

        return Result.success(created)
    }

    suspend fun adminApproveWithdrawal(withdrawalId: Long): Result<Unit> {
        val item = dao.getWithdrawalById(withdrawalId)
            ?: return Result.failure(Exception("Withdrawal request #$withdrawalId not found."))
        if (item.status != "PENDING") {
            return Result.failure(Exception("Only PENDING requests can be approved. Current status: ${item.status}"))
        }

        val updated = item.copy(status = "APPROVED", updatedAt = System.currentTimeMillis())
        dao.updateWithdrawal(updated)
        return Result.success(Unit)
    }

    suspend fun adminRejectWithdrawal(withdrawalId: Long, reason: String): Result<Unit> {
        val item = dao.getWithdrawalById(withdrawalId)
            ?: return Result.failure(Exception("Withdrawal request #$withdrawalId not found."))
        if (item.status == "PAID") {
            return Result.failure(Exception("Cannot reject a withdrawal that has already been PAID."))
        }
        if (item.status == "REJECTED") {
            return Result.failure(Exception("This withdrawal has already been rejected."))
        }

        // Refund coins back to user
        val user = dao.getUserOnce(item.userId)
        if (user != null) {
            val refundedBalance = user.coinsBalance + item.amountCoins
            dao.updateUser(user.copy(coinsBalance = refundedBalance))

            dao.insertTransaction(
                TransactionRecord(
                    userId = item.userId,
                    type = "WITHDRAWAL_REFUND",
                    title = "Withdrawal #$withdrawalId Rejected - Coins Refunded",
                    amountCoins = item.amountCoins,
                    equivalentNaira = item.amountNaira,
                    status = "COMPLETED",
                    reference = "REFUND-WD-$withdrawalId"
                )
            )
        }

        val updated = item.copy(
            status = "REJECTED",
            rejectionReason = reason.ifBlank { "Rejected by administrator during verification." },
            updatedAt = System.currentTimeMillis()
        )
        dao.updateWithdrawal(updated)
        return Result.success(Unit)
    }

    suspend fun adminMarkWithdrawalPaid(
        withdrawalId: Long,
        paymentRef: String,
        note: String
    ): Result<Unit> {
        val item = dao.getWithdrawalById(withdrawalId)
            ?: return Result.failure(Exception("Withdrawal request #$withdrawalId not found."))
        if (item.status == "PAID") {
            return Result.failure(Exception("Security Alert: This withdrawal was ALREADY marked as paid on ${Date(item.paidAt ?: 0)}. Duplicate payouts are strictly prohibited!"))
        }
        if (item.status == "REJECTED") {
            return Result.failure(Exception("Cannot pay a rejected withdrawal request."))
        }

        val finalRef = paymentRef.ifBlank { "TXN-" + UUID.randomUUID().toString().take(10).uppercase() }
        val updated = item.copy(
            status = "PAID",
            paymentReference = finalRef,
            paidAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.updateWithdrawal(updated)

        // Update user's total withdrawn Naira
        val user = dao.getUserOnce(item.userId)
        if (user != null) {
            dao.updateUser(user.copy(totalWithdrawnNaira = user.totalWithdrawnNaira + item.amountNaira))
        }

        dao.insertTransaction(
            TransactionRecord(
                userId = item.userId,
                type = "WITHDRAWAL_PAID",
                title = "Withdrawal Disbursed (₦${String.format(Locale.US, "%,.0f", item.amountNaira)})",
                amountCoins = 0, // already deducted at hold
                equivalentNaira = item.amountNaira,
                status = "COMPLETED",
                reference = finalRef
            )
        )
        return Result.success(Unit)
    }

    suspend fun verifyUserForWithdrawal(userId: String): UserVerificationSummary? {
        val user = dao.getUserOnce(userId) ?: return null
        val pastWithdrawals = dao.getPastWithdrawalsForUser(userId)
        val paidTotal = pastWithdrawals.filter { it.status == "PAID" }.sumOf { it.amountNaira }
        return UserVerificationSummary(
            user = user,
            currentBalance = user.coinsBalance,
            previousWithdrawalsCount = pastWithdrawals.size,
            previousPaidTotalNaira = paidTotal,
            pastRequests = pastWithdrawals
        )
    }

    suspend fun saveConfig(config: AppConfig) {
        dao.saveConfig(config)
    }

    suspend fun toggleOfferAvailability(offerId: String, isAvailable: Boolean) {
        val offer = dao.getOfferById(offerId) ?: return
        dao.updateOffer(offer.copy(isAvailable = isAvailable))
    }

    suspend fun calculateProfitability(
        allUsers: List<UserAccount>,
        allWithdrawals: List<WithdrawalRequest>,
        allTransactions: List<TransactionRecord>,
        config: AppConfig
    ): ProfitabilityStats {
        val totalCoinsIssued = allUsers.sumOf { it.totalEarnedCoins }
        val pendingNaira = allWithdrawals.filter { it.status == "PENDING" || it.status == "APPROVED" }.sumOf { it.amountNaira }
        val paidNaira = allWithdrawals.filter { it.status == "PAID" }.sumOf { it.amountNaira }

        val adCompletions = allTransactions.count { it.type == "REWARDED_AD" }
        val offerCompletions = allTransactions.count { it.type == "APP_OFFER" }

        val estAdRev = adCompletions * config.estimatedAdRevenuePerAdNaira
        val estOfferRev = offerCompletions * config.estimatedOfferMarginNaira
        val totalRevenue = estAdRev + estOfferRev
        val totalLiability = paidNaira + pendingNaira
        val netProfit = totalRevenue - totalLiability

        return ProfitabilityStats(
            totalCoinsIssued = totalCoinsIssued,
            totalWithdrawalsPendingNaira = pendingNaira,
            totalWithdrawalsPaidNaira = paidNaira,
            estimatedAdRevenueNaira = estAdRev,
            estimatedOfferRevenueNaira = estOfferRev,
            totalEstimatedRevenueNaira = totalRevenue,
            totalPayoutLiabilityNaira = totalLiability,
            netEstimatedProfitNaira = netProfit,
            isProfitable = netProfit >= 0
        )
    }
}
