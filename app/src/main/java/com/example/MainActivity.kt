package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RewardedAdDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.EarningScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RewardViewModel
import com.example.ui.viewmodel.UiMessage
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: RewardViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                // Collect state from ViewModel
                val user by viewModel.activeUser.collectAsStateWithLifecycle()
                val config by viewModel.appConfig.collectAsStateWithLifecycle()
                val offers by viewModel.allOffers.collectAsStateWithLifecycle()
                val completedOfferIds by viewModel.completedOfferIds.collectAsStateWithLifecycle()
                val userWithdrawals by viewModel.userWithdrawals.collectAsStateWithLifecycle()
                val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()

                // Admin states
                val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
                val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
                val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
                val profitStats by viewModel.profitabilityStats.collectAsStateWithLifecycle()
                val verificationSummary by viewModel.verificationSummary.collectAsStateWithLifecycle()
                val activeUserId by viewModel.activeUserId.collectAsStateWithLifecycle()

                // Rewarded Ad states
                val isWatchingAd by viewModel.isWatchingAd.collectAsStateWithLifecycle()
                val adCountdown by viewModel.adCountdown.collectAsStateWithLifecycle()
                val canClaimAdReward by viewModel.canClaimAdReward.collectAsStateWithLifecycle()

                // Bottom Nav Tab
                var currentTab by remember { mutableIntStateOf(0) }

                // Pending count for Admin badge
                val pendingWithdrawalCount = allWithdrawals.count { it.status == "PENDING" }

                // Observe Toast / Messages
                LaunchedEffect(Unit) {
                    viewModel.uiMessage.collectLatest { msg ->
                        when (msg) {
                            is UiMessage.Success -> snackbarHostState.showSnackbar(msg.message)
                            is UiMessage.Error -> snackbarHostState.showSnackbar("⚠️ " + msg.message)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "BestRewardNja",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Secure Coins & Payout System",
                                        fontSize = 11.sp,
                                        color = Color(0xFFBCE3CD)
                                    )
                                }
                            },
                            actions = {
                                // Balance quick chip on top bar
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0x33000000),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable { currentTab = 0 }
                                        .testTag("topbar_coins_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = GoldSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format(Locale.US, "%,d", user?.coinsBalance ?: 0L),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = EmeraldDark,
                                titleContentColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = EmeraldPrimary,
                            modifier = Modifier
                                .testTag("main_bottom_nav")
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = "Earn"
                                    )
                                },
                                label = { Text("Earn") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    indicatorColor = GoldSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Withdraw"
                                    )
                                },
                                label = { Text("Withdraw") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    indicatorColor = GoldSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History"
                                    )
                                },
                                label = { Text("History") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    indicatorColor = GoldSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = {
                                    if (pendingWithdrawalCount > 0) {
                                        BadgedBox(badge = {
                                            Badge(containerColor = Color(0xFFE74C3C)) {
                                                Text("$pendingWithdrawalCount")
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = "Admin"
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Admin"
                                        )
                                    }
                                },
                                label = { Text("Admin") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    indicatorColor = GoldSecondary
                                )
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> EarningScreen(
                                user = user,
                                config = config,
                                offers = offers,
                                completedOfferIds = completedOfferIds,
                                onWatchAdClick = { viewModel.startWatchingAd() },
                                onClaimDailyBonus = { viewModel.claimDailyBonus() },
                                onCompleteOffer = { offerId -> viewModel.completeOffer(offerId) },
                                onSubmitReferral = { code -> viewModel.submitReferralCode(code) },
                                onNavigateToWithdraw = { currentTab = 1 }
                            )

                            1 -> WithdrawScreen(
                                user = user,
                                config = config,
                                withdrawals = userWithdrawals,
                                onSubmitWithdrawal = { amount, bank, accNo, accName, method ->
                                    viewModel.submitWithdrawal(amount, bank, accNo, accName, method)
                                }
                            )

                            2 -> HistoryScreen(transactions = userTransactions)

                            3 -> AdminScreen(
                                config = config,
                                users = allUsers,
                                withdrawals = allWithdrawals,
                                transactions = allTransactions,
                                profitStats = profitStats,
                                verificationSummary = verificationSummary,
                                activeUserId = activeUserId,
                                onApproveWithdrawal = { id -> viewModel.adminApproveWithdrawal(id) },
                                onRejectWithdrawal = { id, reason -> viewModel.adminRejectWithdrawal(id, reason) },
                                onMarkPaid = { id, ref, note -> viewModel.adminMarkPaid(id, ref, note) },
                                onVerifyUser = { uId -> viewModel.adminVerifyUser(uId) },
                                onClearVerification = { viewModel.clearVerificationSummary() },
                                onSaveConfig = { cfg -> viewModel.adminUpdateConfig(cfg) },
                                onSwitchUser = { uId -> viewModel.switchActiveUser(uId) }
                            )
                        }

                        // Full-screen Rewarded Ad Simulation Dialog
                        RewardedAdDialog(
                            isOpen = isWatchingAd,
                            countdownSeconds = adCountdown,
                            canClaim = canClaimAdReward,
                            coinsReward = config.coinsPerRewardedAd,
                            onCancel = { viewModel.cancelWatchingAdEarly() },
                            onClaim = { viewModel.finishAndClaimAdReward() }
                        )
                    }
                }
            }
        }
    }
}
