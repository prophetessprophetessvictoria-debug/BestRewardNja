package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppConfig
import com.example.data.model.OfferItem
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.ProfitabilityStats
import com.example.data.repository.RewardRepository
import com.example.data.repository.UserVerificationSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiMessage {
    data class Success(val message: String) : UiMessage()
    data class Error(val message: String) : UiMessage()
}

class RewardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RewardRepository.getInstance(application)

    private val _activeUserId = MutableStateFlow("USR_001")
    val activeUserId: StateFlow<String> = _activeUserId.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UiMessage>()
    val uiMessage = _uiMessage.asSharedFlow()

    // Rewarded Ad Simulation State
    private val _isWatchingAd = MutableStateFlow(false)
    val isWatchingAd: StateFlow<Boolean> = _isWatchingAd.asStateFlow()

    private val _adCountdown = MutableStateFlow(15)
    val adCountdown: StateFlow<Int> = _adCountdown.asStateFlow()

    private val _canClaimAdReward = MutableStateFlow(false)
    val canClaimAdReward: StateFlow<Boolean> = _canClaimAdReward.asStateFlow()

    private var adTimerJob: Job? = null

    val appConfig: StateFlow<AppConfig> = repository.appConfig
        .map { it ?: AppConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppConfig())

    val activeUser: StateFlow<UserAccount?> = _activeUserId.flatMapLatest { userId ->
        repository.getUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTransactions: StateFlow<List<TransactionRecord>> = _activeUserId.flatMapLatest { userId ->
        repository.getTransactionsForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalRequest>> = _activeUserId.flatMapLatest { userId ->
        repository.getWithdrawalsForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedOfferIds: StateFlow<Set<String>> = _activeUserId.flatMapLatest { userId ->
        repository.getCompletedOffersForUser(userId)
    }.map { list -> list.map { it.offerId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allOffers: StateFlow<List<OfferItem>> = repository.allOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin state flows
    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalRequest>> = repository.allWithdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionRecord>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profitabilityStats: StateFlow<ProfitabilityStats?> = combine(
        allUsers,
        allWithdrawals,
        allTransactions,
        appConfig
    ) { users, withdrawals, txs, config ->
        repository.calculateProfitability(users, withdrawals, txs, config)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _verificationSummary = MutableStateFlow<UserVerificationSummary?>(null)
    val verificationSummary: StateFlow<UserVerificationSummary?> = _verificationSummary.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun switchActiveUser(userId: String) {
        _activeUserId.value = userId
    }

    // Rewarded Ad Workflow adhering strictly to user request & AdMob rules:
    // "When a user watches a qualifying rewarded advertisement completely, the system should credit the user's account...
    // Do NOT credit coins simply because the user opens an advertisement."
    fun startWatchingAd() {
        val config = appConfig.value
        if (!config.adsEnabled) {
            viewModelScope.launch {
                _uiMessage.emit(UiMessage.Error("Rewarded video ads are currently disabled by administrator."))
            }
            return
        }

        _isWatchingAd.value = true
        _adCountdown.value = 15
        _canClaimAdReward.value = false

        adTimerJob?.cancel()
        adTimerJob = viewModelScope.launch {
            for (sec in 14 downTo 0) {
                delay(1000)
                _adCountdown.value = sec
            }
            _canClaimAdReward.value = true
        }
    }

    fun cancelWatchingAdEarly() {
        adTimerJob?.cancel()
        _isWatchingAd.value = false
        _canClaimAdReward.value = false
        viewModelScope.launch {
            _uiMessage.emit(UiMessage.Error("Ad closed early. No coins awarded per AdMob policy."))
        }
    }

    fun finishAndClaimAdReward() {
        if (!_canClaimAdReward.value) return
        _isWatchingAd.value = false
        _canClaimAdReward.value = false

        viewModelScope.launch {
            val result = repository.watchAdComplete(_activeUserId.value)
            result.onSuccess { coins ->
                _uiMessage.emit(UiMessage.Success("+$coins Coins credited to your wallet!"))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Failed to credit coins."))
            }
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            val result = repository.claimDailyBonus(_activeUserId.value)
            result.onSuccess { coins ->
                _uiMessage.emit(UiMessage.Success("🎉 Daily Bonus claimed: +$coins Coins!"))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Could not claim daily bonus."))
            }
        }
    }

    fun completeOffer(offerId: String) {
        viewModelScope.launch {
            val result = repository.completeOffer(_activeUserId.value, offerId)
            result.onSuccess { coins ->
                _uiMessage.emit(UiMessage.Success("Task verified! +$coins Coins added to your account."))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Failed to verify task completion."))
            }
        }
    }

    fun submitReferralCode(code: String) {
        viewModelScope.launch {
            val result = repository.applyReferralCode(_activeUserId.value, code)
            result.onSuccess { bonus ->
                _uiMessage.emit(UiMessage.Success("Referral bonus claimed: +$bonus Coins!"))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Invalid referral code."))
            }
        }
    }

    fun submitWithdrawal(
        amountNaira: Double,
        bankName: String,
        accountNumber: String,
        accountName: String,
        payoutMethod: String
    ) {
        viewModelScope.launch {
            val result = repository.requestWithdrawal(
                userId = _activeUserId.value,
                amountNaira = amountNaira,
                bankName = bankName,
                accountNumber = accountNumber,
                accountName = accountName,
                payoutMethod = payoutMethod
            )
            result.onSuccess { req ->
                _uiMessage.emit(UiMessage.Success("Withdrawal request of ₦${String.format(java.util.Locale.US, "%,.0f", req.amountNaira)} submitted! Status: PENDING"))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Withdrawal submission failed."))
            }
        }
    }

    // Administrator Actions
    fun adminApproveWithdrawal(id: Long) {
        viewModelScope.launch {
            val result = repository.adminApproveWithdrawal(id)
            result.onSuccess {
                _uiMessage.emit(UiMessage.Success("Withdrawal #$id approved! Ready for payout."))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Could not approve withdrawal."))
            }
        }
    }

    fun adminRejectWithdrawal(id: Long, reason: String) {
        viewModelScope.launch {
            val result = repository.adminRejectWithdrawal(id, reason)
            result.onSuccess {
                _uiMessage.emit(UiMessage.Success("Withdrawal #$id rejected. Coins refunded to user."))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Could not reject withdrawal."))
            }
        }
    }

    fun adminMarkPaid(id: Long, paymentRef: String, payoutNote: String) {
        viewModelScope.launch {
            val result = repository.adminMarkWithdrawalPaid(id, paymentRef, payoutNote)
            result.onSuccess {
                _uiMessage.emit(UiMessage.Success("Withdrawal #$id marked as PAID. Reference saved permanently."))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Payout processing error."))
            }
        }
    }

    fun adminVerifyUser(userId: String) {
        viewModelScope.launch {
            val summary = repository.verifyUserForWithdrawal(userId)
            _verificationSummary.value = summary
        }
    }

    fun clearVerificationSummary() {
        _verificationSummary.value = null
    }

    fun adminUpdateConfig(newConfig: AppConfig) {
        viewModelScope.launch {
            repository.saveConfig(newConfig)
            _uiMessage.emit(UiMessage.Success("System earning & reward settings updated successfully!"))
        }
    }

    fun adminToggleOffer(offerId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.toggleOfferAvailability(offerId, isAvailable)
            _uiMessage.emit(UiMessage.Success("Offer availability updated."))
        }
    }
}
