package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.UserAccount
import com.example.data.model.WithdrawalRequest
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusRejected
import com.example.ui.theme.StatusRejectedBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val NIGERIAN_BANKS = listOf(
    "Access Bank",
    "GTBank (Guaranty Trust)",
    "Zenith Bank",
    "First Bank of Nigeria",
    "United Bank for Africa (UBA)",
    "Kuda Microfinance Bank",
    "OPay (PayCom)",
    "Palmpay",
    "Moniepoint Microfinance Bank",
    "Fidelity Bank",
    "Stanbic IBTC Bank",
    "Union Bank"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    user: UserAccount?,
    config: AppConfig,
    withdrawals: List<WithdrawalRequest>,
    onSubmitWithdrawal: (amountNaira: Double, bankName: String, accountNo: String, accountName: String, method: String) -> Unit
) {
    var fullName by remember(user?.name) { mutableStateOf(user?.name ?: "") }
    var selectedBank by remember { mutableStateOf(NIGERIAN_BANKS.first()) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var accountNumber by remember { mutableStateOf("") }
    var withdrawalAmountStr by remember { mutableStateOf("1000") }
    var selectedPayoutMethod by remember { mutableStateOf("Bank Transfer") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val userCoins = user?.coinsBalance ?: 0L
    val enteredAmountNaira = withdrawalAmountStr.toDoubleOrNull() ?: 0.0
    val requiredCoins = (enteredAmountNaira / config.coinsToNairaRate).toLong()
    val hasEnoughCoins = userCoins >= requiredCoins && requiredCoins > 0
    val meetsMinimum = enteredAmountNaira >= config.minWithdrawalNaira

    val payoutMethods = listOf("Bank Transfer", "OPay Wallet", "Palmpay Wallet")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("withdraw_screen_scroll"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        // Balance & Threshold Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Withdrawable Balance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Min: ₦${String.format(Locale.US, "%,.0f", config.minWithdrawalNaira)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "%,d Coins", userCoins),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "≈ ₦${String.format(Locale.US, "%,.2f", userCoins * config.coinsToNairaRate)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldDark
                        )
                    }

                    if (!config.withdrawalsEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Notice: Withdrawals are temporarily paused for maintenance by administrator.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Form Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Request Cash Withdrawal",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Funds will be transferred directly to your account upon administrator verification",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payout Method Selector
                    Text(
                        text = "Payout Channel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        payoutMethods.forEach { method ->
                            FilterChip(
                                selected = selectedPayoutMethod == method,
                                onClick = {
                                    selectedPayoutMethod = method
                                    if (method == "OPay Wallet") selectedBank = "OPay (PayCom)"
                                    if (method == "Palmpay Wallet") selectedBank = "Palmpay"
                                },
                                label = { Text(text = method, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Account Holder Full Name") },
                        placeholder = { Text("e.g. Chidi Eze") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_fullname_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bank Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = bankDropdownExpanded,
                        onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Bank / Fintech") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("withdraw_bank_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = bankDropdownExpanded,
                            onDismissRequest = { bankDropdownExpanded = false }
                        ) {
                            NIGERIAN_BANKS.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank) },
                                    onClick = {
                                        selectedBank = bank
                                        bankDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Number
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = {
                            if (it.length <= 11) accountNumber = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Account Number / NUBAN (10 digits)") },
                        placeholder = { Text("e.g. 0123456789") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_number_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount Section
                    Text(
                        text = "Withdrawal Amount (₦)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf(1000, 2000, 5000, 10000)) { chipVal ->
                            FilterChip(
                                selected = enteredAmountNaira.toInt() == chipVal,
                                onClick = { withdrawalAmountStr = chipVal.toString() },
                                label = { Text("₦${String.format(Locale.US, "%,d", chipVal)}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldSecondary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = withdrawalAmountStr,
                        onValueChange = { withdrawalAmountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Amount (Minimum ₦${String.format(Locale.US, "%,.0f", config.minWithdrawalNaira)})") },
                        prefix = { Text("₦ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coin Cost Calculation Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cost in Coins:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$requiredCoins Coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasEnoughCoins) EmeraldPrimary else MaterialTheme.colorScheme.error
                        )
                    }

                    // Validation Message if any
                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = validationError ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (!meetsMinimum && enteredAmountNaira > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Amount is below the minimum withdrawal of ₦${String.format(Locale.US, "%,.0f", config.minWithdrawalNaira)}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (!hasEnoughCoins && enteredAmountNaira > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Insufficient coins. You need $requiredCoins coins, but have $userCoins coins.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            validationError = null
                            if (fullName.isBlank()) {
                                validationError = "Please enter the account holder name."
                                return@Button
                            }
                            if (accountNumber.length < 10) {
                                validationError = "Please enter a valid 10-digit account number."
                                return@Button
                            }
                            if (!meetsMinimum) {
                                validationError = "Minimum withdrawal amount is ₦${String.format(Locale.US, "%,.0f", config.minWithdrawalNaira)}."
                                return@Button
                            }
                            if (!hasEnoughCoins) {
                                validationError = "Insufficient coins balance to withdraw this amount."
                                return@Button
                            }
                            showConfirmDialog = true
                        },
                        enabled = config.withdrawalsEnabled && meetsMinimum && hasEnoughCoins && accountNumber.length >= 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_withdrawal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Withdrawal Request",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Section: Withdrawal Status History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Withdrawal Requests",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${withdrawals.size} Total",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (withdrawals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No withdrawal requests yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Earn coins and submit your first payout request above!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(withdrawals) { req ->
                WithdrawalRequestCard(req = req)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(text = "Confirm Withdrawal Request")
            },
            text = {
                Column {
                    Text(
                        text = "Please verify your payout destination carefully:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Amount: ₦${String.format(Locale.US, "%,.2f", enteredAmountNaira)} ($requiredCoins Coins)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Name: $fullName", fontSize = 13.sp)
                            Text(text = "Bank: $selectedBank", fontSize = 13.sp)
                            Text(text = "Account: $accountNumber", fontSize = 13.sp)
                            Text(text = "Method: $selectedPayoutMethod", fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Once submitted, $requiredCoins coins will be held until the administrator approves and disburses the transfer.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onSubmitWithdrawal(
                            enteredAmountNaira,
                            selectedBank,
                            accountNumber,
                            fullName,
                            selectedPayoutMethod
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Confirm & Submit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WithdrawalRequestCard(req: WithdrawalRequest) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.requestedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_card_${req.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Amount & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₦" + String.format(Locale.US, "%,.2f", req.amountNaira),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${req.amountCoins} Coins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(status = req.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bank details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${req.bankName} • ${req.accountNumber} (${req.accountName})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date
            Text(
                text = "Requested: $dateStr",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Rejection reason or Payment Ref
            if (req.status == "REJECTED" && !req.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusRejectedBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = StatusRejected,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reason: ${req.rejectionReason} (Coins Refunded)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = StatusRejected
                        )
                    }
                }
            } else if (req.status == "PAID") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusPaidBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusPaid,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Payment Ref: ${req.paymentReference ?: "Paid"} • Transferred to bank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = StatusPaid
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val (bgColor, textColor, text) = when (status) {
        "PENDING" -> Triple(StatusPendingBg, StatusPending, "⏳ Pending")
        "APPROVED" -> Triple(StatusApprovedBg, StatusApproved, "✓ Approved")
        "PAID" -> Triple(StatusPaidBg, StatusPaid, "💰 Paid")
        "REJECTED" -> Triple(StatusRejectedBg, StatusRejected, "✖ Rejected")
        else -> Triple(Color(0xFFEEEEEE), Color.Gray, status)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
