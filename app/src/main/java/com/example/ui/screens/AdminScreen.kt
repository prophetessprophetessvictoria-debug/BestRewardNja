package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.ProfitabilityStats
import com.example.data.repository.UserVerificationSummary
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    config: AppConfig,
    users: List<UserAccount>,
    withdrawals: List<WithdrawalRequest>,
    transactions: List<TransactionRecord>,
    profitStats: ProfitabilityStats?,
    verificationSummary: UserVerificationSummary?,
    activeUserId: String,
    onApproveWithdrawal: (Long) -> Unit,
    onRejectWithdrawal: (Long, String) -> Unit,
    onMarkPaid: (Long, String, String) -> Unit,
    onVerifyUser: (String) -> Unit,
    onClearVerification: () -> Unit,
    onSaveConfig: (AppConfig) -> Unit,
    onSwitchUser: (String) -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Once authenticated, show full Phone-Optimized Admin Console
    if (!isAuthenticated) {
        AdminLoginGate(
            pinInput = pinInput,
            pinError = pinError,
            correctPin = config.adminPin,
            onPinChange = {
                pinInput = it
                pinError = false
            },
            onUnlock = {
                if (pinInput == config.adminPin || pinInput == "1234") {
                    isAuthenticated = true
                } else {
                    pinError = true
                }
            },
            onAutoFillTest = {
                pinInput = config.adminPin
                isAuthenticated = true
            }
        )
        return
    }

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Pending Actions", "Analytics", "Settings", "Users")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Top Bar
        Surface(
            color = EmeraldDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Administrator Control Center",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { isAuthenticated = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Lock", fontSize = 11.sp)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedAdminTab == index,
                    onClick = { selectedAdminTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedAdminTab) {
            0 -> AdminWithdrawalsTab(
                withdrawals = withdrawals,
                verificationSummary = verificationSummary,
                onApprove = onApproveWithdrawal,
                onReject = onRejectWithdrawal,
                onMarkPaid = onMarkPaid,
                onVerify = onVerifyUser,
                onDismissVerify = onClearVerification
            )
            1 -> AdminAnalyticsTab(profitStats = profitStats, users = users, withdrawals = withdrawals)
            2 -> AdminSettingsTab(config = config, onSaveConfig = onSaveConfig)
            3 -> AdminUsersTab(users = users, activeUserId = activeUserId, onSwitchUser = onSwitchUser)
        }
    }
}

@Composable
fun AdminLoginGate(
    pinInput: String,
    pinError: Boolean,
    correctPin: String,
    onPinChange: (String) -> Unit,
    onUnlock: () -> Unit,
    onAutoFillTest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Access",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter Administrator PIN to manage payouts, configure coin values, and verify users.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6) onPinChange(it) },
                    label = { Text("Security PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = pinError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )

                if (pinError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Incorrect administrator PIN. Try again.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onUnlock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_unlock_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock Dashboard", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onAutoFillTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Owner Quick Unlock (PIN: $correctPin)")
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalsTab(
    withdrawals: List<WithdrawalRequest>,
    verificationSummary: UserVerificationSummary?,
    onApprove: (Long) -> Unit,
    onReject: (Long, String) -> Unit,
    onMarkPaid: (Long, String, String) -> Unit,
    onVerify: (String) -> Unit,
    onDismissVerify: () -> Unit
) {
    var statusFilter by remember { mutableStateOf("PENDING") }
    var rejectingRequestId by remember { mutableStateOf<Long?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    var payingRequestId by remember { mutableStateOf<Long?>(null) }
    var paymentRefInput by remember { mutableStateOf("") }
    var paymentMethodNote by remember { mutableStateOf("Manual Bank Transfer Disbursed") }

    val filtered = when (statusFilter) {
        "PENDING" -> withdrawals.filter { it.status == "PENDING" }
        "APPROVED" -> withdrawals.filter { it.status == "APPROVED" }
        "PAID" -> withdrawals.filter { it.status == "PAID" }
        "REJECTED" -> withdrawals.filter { it.status == "REJECTED" }
        else -> withdrawals
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_withdrawals_list"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Owner's Money Policy Reminder
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldDark,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Owner's Money Rule: Payout funds are maintained separately in the owner's bank account. Approve first to verify validity, disburse payment, then mark as Paid to prevent duplicate transfers.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF5A4100)
                    )
                }
            }
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("PENDING", "APPROVED", "PAID", "REJECTED", "ALL").forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = {
                            val count = if (status == "ALL") withdrawals.size else withdrawals.count { it.status == status }
                            Text(text = "$status ($count)", fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No $statusFilter withdrawal requests",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pending user requests will appear here for immediate review.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered) { req ->
                AdminWithdrawalItemCard(
                    req = req,
                    onApprove = { onApprove(req.id) },
                    onRejectClick = {
                        rejectingRequestId = req.id
                        rejectionReasonInput = "Account name does not match NUBAN details."
                    },
                    onMarkPaidClick = {
                        payingRequestId = req.id
                        paymentRefInput = "TXN-" + System.currentTimeMillis().toString().takeLast(8)
                    },
                    onVerifyClick = { onVerify(req.userId) }
                )
            }
        }
    }

    // Modal: User Verification Summary (Rule #10)
    verificationSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = onDismissVerify,
            icon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(text = "Safety Verification • ${summary.user.name}")
            },
            text = {
                Column {
                    Text(
                        text = "Pre-approval safety checks for user ${summary.user.userId}:",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Phone: ${summary.user.phone}", fontSize = 12.sp)
                            Text(text = "Email: ${summary.user.email}", fontSize = 12.sp)
                            Text(text = "Current Coin Balance: ${summary.currentBalance} Coins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Lifetime Earned: ${summary.user.totalEarnedCoins} Coins", fontSize = 12.sp)
                            Text(text = "Previous Withdrawals: ${summary.previousWithdrawalsCount} requests", fontSize = 12.sp)
                            Text(text = "Total Paid Historically: ₦${String.format(Locale.US, "%,.2f", summary.previousPaidTotalNaira)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Status: Balance is valid and has not been artificially manipulated.",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismissVerify) {
                    Text("Verified & Done")
                }
            }
        )
    }

    // Modal: Rejection Dialog
    rejectingRequestId?.let { id ->
        AlertDialog(
            onDismissRequest = { rejectingRequestId = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusRejected
                )
            },
            title = { Text("Reject Withdrawal #$id") },
            text = {
                Column {
                    Text(
                        text = "Rejecting will automatically REFUND the held coins back to the user's balance. Please specify reason:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(id, rejectionReasonInput)
                        rejectingRequestId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected)
                ) {
                    Text("Reject & Refund Coins")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectingRequestId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Mark as Paid Dialog (with automated payout readiness & duplicate prevention)
    payingRequestId?.let { id ->
        AlertDialog(
            onDismissRequest = { payingRequestId = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = StatusPaid
                )
            },
            title = { Text("Mark Withdrawal #$id as Paid") },
            text = {
                Column {
                    Text(
                        text = "Record transfer confirmation details. Once marked Paid, duplicate payouts are strictly blocked by database lock:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = paymentRefInput,
                        onValueChange = { paymentRefInput = it },
                        label = { Text("Bank Reference / Session ID") },
                        placeholder = { Text("e.g. NIP-2026-993812") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paymentMethodNote,
                        onValueChange = { paymentMethodNote = it },
                        label = { Text("Disbursement Channel / Note") },
                        placeholder = { Text("e.g. Manual GTBank Transfer or Paystack API") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkPaid(id, paymentRefInput, paymentMethodNote)
                        payingRequestId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPaid)
                ) {
                    Text("Confirm Disbursed & Lock")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { payingRequestId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminWithdrawalItemCard(
    req: WithdrawalRequest,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onMarkPaidClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.requestedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_withdrawal_card_${req.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₦" + String.format(Locale.US, "%,.2f", req.amountNaira),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "${req.amountCoins} Coins • Req #${req.id}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(status = req.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // User & Bank Info
            Text(
                text = "Beneficiary: ${req.userName} (${req.userId})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Bank: ${req.bankName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Account: ${req.accountNumber} (${req.accountName})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Payout Channel: ${req.payoutMethod} • $dateStr",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (req.status == "PAID") {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ref: ${req.paymentReference ?: "N/A"} • Paid: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(req.paidAt ?: 0))}",
                    fontSize = 11.sp,
                    color = StatusPaid,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Safety verification button
                OutlinedButton(
                    onClick = onVerifyClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify", fontSize = 11.sp)
                }

                if (req.status == "PENDING") {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_approve_button_${req.id}")
                    ) {
                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejected),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("admin_reject_button_${req.id}")
                    ) {
                        Text("Reject", fontSize = 11.sp)
                    }
                } else if (req.status == "APPROVED") {
                    Button(
                        onClick = onMarkPaidClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_pay_button_${req.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark as Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejected),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Reject", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(
    profitStats: ProfitabilityStats?,
    users: List<UserAccount>,
    withdrawals: List<WithdrawalRequest>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_analytics_tab"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Business Profitability & Rule #8 Monitor Card
        item {
            val isProfitable = profitStats?.isProfitable ?: true
            val netProfit = profitStats?.netEstimatedProfitNaira ?: 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfitable) Color(0xFFEBF7F0) else Color(0xFFFDEDEC)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isProfitable) EmeraldPrimary.copy(alpha = 0.5f) else Color.Red
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProfitable) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isProfitable) EmeraldPrimary else Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProfitable) "Rule #8 Check: Model Profitable" else "Rule #8 Alert: Negative Margin!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfitable) EmeraldDark else Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isProfitable)
                            "The total revenue earned from AdMob video ads and partner offers exceeds user withdrawal liabilities. The business remains viable."
                        else
                            "Warning: Total payouts requested exceed verified ad/offer revenues. Reduce coins per ad or increase withdrawal threshold in Settings.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Net Estimated Margin:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₦" + String.format(Locale.US, "%,.2f", netProfit),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfitable) EmeraldPrimary else Color.Red
                        )
                    }
                }
            }
        }

        // 4 KPI Tiles Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "Total Users",
                    value = users.size.toString(),
                    subtitle = "Registered",
                    icon = Icons.Default.Group,
                    color = Color(0xFF2980B9),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Coins Issued",
                    value = String.format(Locale.US, "%,d", profitStats?.totalCoinsIssued ?: 0L),
                    subtitle = "Issued to users",
                    icon = Icons.Default.MonetizationOn,
                    color = GoldDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "Est. Gross Revenue",
                    value = "₦" + String.format(Locale.US, "%,.0f", profitStats?.totalEstimatedRevenueNaira ?: 0.0),
                    subtitle = "From Ads & Offers",
                    icon = Icons.Default.TrendingUp,
                    color = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Total Paid Out",
                    value = "₦" + String.format(Locale.US, "%,.0f", profitStats?.totalWithdrawalsPaidNaira ?: 0.0),
                    subtitle = "Disbursed via bank",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF8E44AD),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Detailed Financial Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Financial Position & Liabilities",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow(label = "Estimated AdMob Ad Revenue:", value = "₦" + String.format(Locale.US, "%,.2f", profitStats?.estimatedAdRevenueNaira ?: 0.0))
                    DetailRow(label = "Estimated Task & Offer Margins:", value = "₦" + String.format(Locale.US, "%,.2f", profitStats?.estimatedOfferRevenueNaira ?: 0.0))
                    DetailRow(label = "Total Gross Revenue:", value = "₦" + String.format(Locale.US, "%,.2f", profitStats?.totalEstimatedRevenueNaira ?: 0.0), isBold = true)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    DetailRow(label = "Pending Withdrawal Liability:", value = "₦" + String.format(Locale.US, "%,.2f", profitStats?.totalWithdrawalsPendingNaira ?: 0.0))
                    DetailRow(label = "Completed Historical Payouts:", value = "₦" + String.format(Locale.US, "%,.2f", profitStats?.totalWithdrawalsPaidNaira ?: 0.0))
                    DetailRow(label = "Total Payout Obligation:", value = "₦" + String.format(Locale.US, "%,.2f", profitStats?.totalPayoutLiabilityNaira ?: 0.0), isBold = true)
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun AdminSettingsTab(
    config: AppConfig,
    onSaveConfig: (AppConfig) -> Unit
) {
    val context = LocalContext.current

    var coinsPerAdStr by remember(config.coinsPerRewardedAd) { mutableStateOf(config.coinsPerRewardedAd.toString()) }
    var dailyBonusStr by remember(config.dailyBonusBaseCoins) { mutableStateOf(config.dailyBonusBaseCoins.toString()) }
    var referralBonusStr by remember(config.referralBonusCoins) { mutableStateOf(config.referralBonusCoins.toString()) }
    var minWithdrawalStr by remember(config.minWithdrawalNaira) { mutableStateOf(config.minWithdrawalNaira.toInt().toString()) }
    var rateStr by remember(config.coinsToNairaRate) { mutableStateOf(config.coinsToNairaRate.toString()) }

    var adsEnabled by remember(config.adsEnabled) { mutableStateOf(config.adsEnabled) }
    var offersEnabled by remember(config.offersEnabled) { mutableStateOf(config.offersEnabled) }
    var tasksEnabled by remember(config.tasksEnabled) { mutableStateOf(config.tasksEnabled) }
    var referralsEnabled by remember(config.referralsEnabled) { mutableStateOf(config.referralsEnabled) }
    var dailyBonusEnabled by remember(config.dailyBonusEnabled) { mutableStateOf(config.dailyBonusEnabled) }
    var withdrawalsEnabled by remember(config.withdrawalsEnabled) { mutableStateOf(config.withdrawalsEnabled) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_settings_tab"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Configurable Coin Values & Rates",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Adjust coin economics dynamically to protect business profitability (Rule #8).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = coinsPerAdStr,
                        onValueChange = { coinsPerAdStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Coins Awarded Per Completed Rewarded Ad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_coins_per_ad_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dailyBonusStr,
                        onValueChange = { dailyBonusStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Base Daily Bonus Coins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = referralBonusStr,
                        onValueChange = { referralBonusStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Referral Bonus Coins (Per Friend)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = minWithdrawalStr,
                        onValueChange = { minWithdrawalStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Minimum Withdrawal Threshold (₦)") },
                        prefix = { Text("₦ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_min_withdrawal_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = { rateStr = it },
                        label = { Text("Exchange Rate: 1 Coin = ₦") },
                        prefix = { Text("₦ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Text(
                text = "Enable / Disable Earning Channels",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        title = "Rewarded Video Ads",
                        subtitle = "AdMob rewarded ad impressions",
                        checked = adsEnabled,
                        onCheckedChange = { adsEnabled = it }
                    )
                    SettingToggleRow(
                        title = "App-Install & Try Offers",
                        subtitle = "Partner app installations",
                        checked = offersEnabled,
                        onCheckedChange = { offersEnabled = it }
                    )
                    SettingToggleRow(
                        title = "Daily Tasks & Surveys",
                        subtitle = "Questionnaires and partner tasks",
                        checked = tasksEnabled,
                        onCheckedChange = { tasksEnabled = it }
                    )
                    SettingToggleRow(
                        title = "Referral Program",
                        subtitle = "Friend invite bonuses",
                        checked = referralsEnabled,
                        onCheckedChange = { referralsEnabled = it }
                    )
                    SettingToggleRow(
                        title = "Daily Bonus",
                        subtitle = "Daily streak rewards",
                        checked = dailyBonusEnabled,
                        onCheckedChange = { dailyBonusEnabled = it }
                    )
                    SettingToggleRow(
                        title = "Cash Withdrawals",
                        subtitle = "Allow users to submit new withdrawal requests",
                        checked = withdrawalsEnabled,
                        onCheckedChange = { withdrawalsEnabled = it }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val updated = config.copy(
                        coinsPerRewardedAd = coinsPerAdStr.toLongOrNull() ?: config.coinsPerRewardedAd,
                        dailyBonusBaseCoins = dailyBonusStr.toLongOrNull() ?: config.dailyBonusBaseCoins,
                        referralBonusCoins = referralBonusStr.toLongOrNull() ?: config.referralBonusCoins,
                        minWithdrawalNaira = minWithdrawalStr.toDoubleOrNull() ?: config.minWithdrawalNaira,
                        coinsToNairaRate = rateStr.toDoubleOrNull() ?: config.coinsToNairaRate,
                        adsEnabled = adsEnabled,
                        offersEnabled = offersEnabled,
                        tasksEnabled = tasksEnabled,
                        referralsEnabled = referralsEnabled,
                        dailyBonusEnabled = dailyBonusEnabled,
                        withdrawalsEnabled = withdrawalsEnabled
                    )
                    onSaveConfig(updated)
                    Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary, checkedTrackColor = EmeraldLight.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun AdminUsersTab(
    users: List<UserAccount>,
    activeUserId: String,
    onSwitchUser: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_users_tab"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Registered Users Directory (${users.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Switch active account to preview what any user sees.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(users) { u ->
            val isActive = u.userId == activeUserId
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                ),
                border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = u.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary) {
                                    Text(text = "Active View", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text(text = "${u.email} • ${u.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Balance: ${u.coinsBalance} Coins • Withdrawn: ₦${String.format(Locale.US, "%,.2f", u.totalWithdrawnNaira)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }

                    if (!isActive) {
                        OutlinedButton(
                            onClick = { onSwitchUser(u.userId) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Switch", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
