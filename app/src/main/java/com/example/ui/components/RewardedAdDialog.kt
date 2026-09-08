package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary

@Composable
fun RewardedAdDialog(
    isOpen: Boolean,
    countdownSeconds: Int,
    canClaim: Boolean,
    coinsReward: Long,
    onCancel: () -> Unit,
    onClaim: () -> Unit
) {
    if (!isOpen) return

    var showExitWarning by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (canClaim) {
                onClaim()
            } else {
                showExitWarning = true
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("rewarded_ad_player"),
            color = Color(0xFF0F1713)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Simulated Video Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF003820),
                                    Color(0xFF08140E),
                                    Color(0xFF14241B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F372B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (canClaim) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = "Ad Media Playing",
                                tint = if (canClaim) EmeraldLight else GoldSecondary,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = if (canClaim) "Ad Complete!" else "Fintech Nigeria • Instant Payouts",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (canClaim)
                                "You have fulfilled the complete watch requirement.\nClaim your reward below!"
                            else
                                "Watch this sponsored presentation completely to earn $coinsReward coins for your BestRewardNja wallet.",
                            fontSize = 14.sp,
                            color = Color(0xFFA0B3A8),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Progress Indicator
                        val progress = ((15 - countdownSeconds) / 15f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (canClaim) EmeraldLight else GoldSecondary,
                            trackColor = Color(0xFF2C4437)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!canClaim) {
                            Text(
                                text = "Reward in: $countdownSeconds seconds",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldSecondary
                            )
                        } else {
                            Text(
                                text = "🎉 +$coinsReward Coins Ready to Claim!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldLight
                            )
                        }
                    }
                }

                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x99000000)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AdMob Verified Rewarded Ad",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (canClaim) {
                                onClaim()
                            } else {
                                showExitWarning = true
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("close_ad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Action Button (When complete)
                AnimatedVisibility(
                    visible = canClaim,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("claim_ad_reward_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Claim +$coinsReward Coins",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Exit Warning Dialog to prevent accidental skips & enforce AdMob policies
    if (showExitWarning) {
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = GoldSecondary
                )
            },
            title = {
                Text(text = "Leave Ad Early?")
            },
            text = {
                Text(
                    text = "If you close this rewarded ad before it finishes ($countdownSeconds seconds remaining), you will NOT earn the $coinsReward coins per official AdMob policy."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitWarning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Resume Video")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExitWarning = false
                        onCancel()
                    }
                ) {
                    Text("Quit & Forfeit")
                }
            }
        )
    }
}
