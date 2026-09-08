package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppConfig
import com.example.data.model.OfferItem
import com.example.data.model.UserAccount
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EarningScreen(
    user: UserAccount?,
    config: AppConfig,
    offers: List<OfferItem>,
    completedOfferIds: Set<String>,
    onWatchAdClick: () -> Unit,
    onClaimDailyBonus: () -> Unit,
    onCompleteOffer: (String) -> Unit,
    onSubmitReferral: (String) -> Unit,
    onNavigateToWithdraw: () -> Unit
) {
    val context = LocalContext.current
    var selectedOfferForDetails by remember { mutableStateOf<OfferItem?>(null) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showOffersSheet by remember { mutableStateOf(false) }
    var showTasksSheet by remember { mutableStateOf(false) }

    val coinsBalance = user?.coinsBalance ?: 0L
    val nairaValue = coinsBalance * config.coinsToNairaRate

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isDailyBonusClaimedToday = user?.lastDailyBonusDate == todayDate

    // Filter legitimate partner offers
    val availableAppOffers = offers.filter {
        it.category in listOf("APP_INSTALL", "APP_TRY") && it.isAvailable
    }
    val availableTaskOffers = offers.filter {
        it.category in listOf("TASK", "SURVEY") && it.isAvailable
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("earning_screen_scroll"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Balance Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                EmeraldDark,
                                EmeraldPrimary,
                                Color(0xFF065A37)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFC7EBD7)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format(Locale.US, "%,d", coinsBalance),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = " Coins",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD4EEDF),
                                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                                )
                            }
                        }

                        // Cash Equivalent
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33000000)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Estimated Cash",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBCE3CD)
                                )
                                Text(
                                    text = "₦" + String.format(Locale.US, "%,.2f", nairaValue),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rate: 1 Coin = ₦${String.format(Locale.US, "%.2f", config.coinsToNairaRate)}",
                            fontSize = 12.sp,
                            color = Color(0xFFB8E2CA)
                        )

                        Button(
                            onClick = onNavigateToWithdraw,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("quick_withdraw_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Withdraw",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.reward_hero_banner),
                        contentDescription = "Rewards Hero Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xCC002816),
                                        Color(0x33002816)
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = "Earn & Withdraw in Naira",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Complete genuine tasks & withdraw directly to your bank account",
                                fontSize = 12.sp,
                                color = Color(0xFFD4EEDF)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Earn Coins
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Earn Coins",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "5 Earning Methods",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // 1. 🎬 Watch Rewarded Ad
        item {
            EarningOptionCard(
                icon = Icons.Default.PlayCircleFilled,
                iconTint = Color(0xFFE74C3C),
                title = "Watch Rewarded Ad",
                subtitle = "Watch qualifying sponsored videos completely per AdMob policy",
                rewardBadge = "+${config.coinsPerRewardedAd} Coins",
                isAvailable = config.adsEnabled,
                statusText = if (config.adsEnabled) "Available Now" else "Disabled by Admin",
                testTag = "earn_card_rewarded_ad",
                onClick = {
                    if (config.adsEnabled) {
                        onWatchAdClick()
                    } else {
                        Toast.makeText(context, "Rewarded ads are currently paused by administrator.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 2. 📱 Try Available Apps/Offers
        item {
            val offersAvailable = config.offersEnabled && availableAppOffers.isNotEmpty()
            EarningOptionCard(
                icon = Icons.Default.Smartphone,
                iconTint = EmeraldPrimary,
                title = "Try Available Apps/Offers",
                subtitle = "Install & test verified partner apps (OPay, Kuda Bank, etc.)",
                rewardBadge = "Up to 350 Coins",
                isAvailable = offersAvailable,
                statusText = if (offersAvailable) "${availableAppOffers.size} Offers Active" else "No Offers Available",
                testTag = "earn_card_app_offers",
                onClick = {
                    showOffersSheet = true
                }
            )
        }

        // 3. 📝 Complete Tasks
        item {
            val tasksAvailable = config.tasksEnabled && availableTaskOffers.isNotEmpty()
            EarningOptionCard(
                icon = Icons.Default.Task,
                iconTint = Color(0xFF2980B9),
                title = "Complete Tasks",
                subtitle = "Participate in legitimate market surveys and financial research",
                rewardBadge = "Up to 500 Coins",
                isAvailable = tasksAvailable,
                statusText = if (tasksAvailable) "${availableTaskOffers.size} Tasks Active" else "No Tasks Available",
                testTag = "earn_card_tasks",
                onClick = {
                    showTasksSheet = true
                }
            )
        }

        // 4. 👥 Refer Friends
        item {
            EarningOptionCard(
                icon = Icons.Default.Group,
                iconTint = Color(0xFF8E44AD),
                title = "Refer Friends",
                subtitle = "Share your invite code with friends & family",
                rewardBadge = "+${config.referralBonusCoins} Coins / Friend",
                isAvailable = config.referralsEnabled,
                statusText = if (config.referralsEnabled) "Code: ${user?.referralCode ?: "Active"}" else "Disabled",
                testTag = "earn_card_refer_friends",
                onClick = {
                    showReferralDialog = true
                }
            )
        }

        // 5. 🎁 Daily Bonus
        item {
            val streak = user?.dailyBonusStreak ?: 0
            val todayReward = config.dailyBonusBaseCoins + (streak.coerceAtMost(7) * 10L)
            EarningOptionCard(
                icon = Icons.Default.CardGiftcard,
                iconTint = GoldSecondary,
                title = "Daily Bonus",
                subtitle = "Day $streak streak • Claim everyday to maximize bonus coins",
                rewardBadge = "+$todayReward Coins",
                isAvailable = config.dailyBonusEnabled && !isDailyBonusClaimedToday,
                statusText = if (!config.dailyBonusEnabled) "Disabled" else if (isDailyBonusClaimedToday) "Claimed Today" else "Ready to Claim",
                testTag = "earn_card_daily_bonus",
                onClick = {
                    if (config.dailyBonusEnabled && !isDailyBonusClaimedToday) {
                        onClaimDailyBonus()
                    } else if (isDailyBonusClaimedToday) {
                        Toast.makeText(context, "You've already claimed your daily bonus today! Return tomorrow.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Daily bonus is currently disabled by administrator.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Trust & Security Notice Footer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "BestRewardNja processes real earnings from verified ad views & sponsor offers. Balance can be withdrawn to any Nigerian bank once threshold is met.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal: Available App Offers Sheet
    if (showOffersSheet) {
        OffersListDialog(
            title = "Try Available Apps & Offers",
            category = "APP",
            offers = availableAppOffers,
            completedIds = completedOfferIds,
            onDismiss = { showOffersSheet = false },
            onSelectOffer = { offer ->
                selectedOfferForDetails = offer
            }
        )
    }

    // Modal: Available Tasks Sheet
    if (showTasksSheet) {
        OffersListDialog(
            title = "Complete Daily Tasks",
            category = "TASK",
            offers = availableTaskOffers,
            completedIds = completedOfferIds,
            onDismiss = { showTasksSheet = false },
            onSelectOffer = { offer ->
                selectedOfferForDetails = offer
            }
        )
    }

    // Offer Details & Verification Dialog
    selectedOfferForDetails?.let { offer ->
        val isCompleted = completedOfferIds.contains(offer.id)
        AlertDialog(
            onDismissRequest = { selectedOfferForDetails = null },
            icon = {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.LocalActivity,
                    contentDescription = null,
                    tint = if (isCompleted) EmeraldPrimary else GoldSecondary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = offer.title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Provider: ${offer.provider}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${offer.rewardCoins} Coins",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = offer.description,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Verification Instructions:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = offer.instructions,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isCompleted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "✓ You have already completed this offer and received ${offer.rewardCoins} coins. Offers cannot be repeated.",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (!isCompleted) {
                    Button(
                        onClick = {
                            selectedOfferForDetails = null
                            onCompleteOffer(offer.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Verify & Claim ${offer.rewardCoins} Coins")
                    }
                } else {
                    Button(
                        onClick = { selectedOfferForDetails = null }
                    ) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (!isCompleted) {
                    OutlinedButton(onClick = { selectedOfferForDetails = null }) {
                        Text("Later")
                    }
                }
            }
        )
    }

    // Modal: Referral Dialog
    if (showReferralDialog) {
        var inputCode by remember { mutableStateOf("") }
        val myCode = user?.referralCode ?: "BRN-1001"

        AlertDialog(
            onDismissRequest = { showReferralDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(text = "Refer Friends & Earn Coins")
            },
            text = {
                Column {
                    Text(
                        text = "Share your personal invitation code. When a friend joins and enters your code, you both earn ${config.referralBonusCoins} Coins!",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Referral Code:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = myCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = EmeraldPrimary
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Referral Code", myCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Referral code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = EmeraldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (user?.referredBy == null) {
                        Text(
                            text = "Have a Friend's Referral Code?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it.uppercase() },
                            placeholder = { Text("e.g. BRN-CHIDI99") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ You were referred by: ${user.referredBy}",
                                fontSize = 12.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (user?.referredBy == null && inputCode.isNotBlank()) {
                    Button(
                        onClick = {
                            onSubmitReferral(inputCode.trim())
                            showReferralDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Redeem Code")
                    }
                } else {
                    Button(onClick = { showReferralDialog = false }) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReferralDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun EarningOptionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    rewardBadge: String,
    isAvailable: Boolean,
    statusText: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(testTag)
            .clickable(enabled = true, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAvailable) 2.dp else 0.dp),
        border = if (isAvailable) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Status pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isAvailable) EmeraldPrimary else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isAvailable) EmeraldPrimary else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Reward pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isAvailable) EmeraldLight.copy(alpha = 0.15f) else Color(0x1A000000)
            ) {
                Text(
                    text = rewardBadge,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isAvailable) EmeraldPrimary else Color.Gray,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun OffersListDialog(
    title: String,
    category: String,
    offers: List<OfferItem>,
    completedIds: Set<String>,
    onDismiss: () -> Unit,
    onSelectOffer: (OfferItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            if (offers.isEmpty()) {
                // Requirement 9: If there are no offers available, show:
                // "No offers are currently available. Please check again later."
                // Do not show fake offers.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No offers are currently available. Please check again later.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(offers) { offer ->
                        val isDone = completedIds.contains(offer.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable { onSelectOffer(offer) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = offer.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "By ${offer.provider}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isDone) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Completed",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimary
                                    ) {
                                        Text(
                                            text = "+${offer.rewardCoins}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
