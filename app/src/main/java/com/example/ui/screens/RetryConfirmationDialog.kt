package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GamingButton
import com.example.ui.theme.*

@Composable
fun RetryConfirmationDialog(
    levelNumber: Int,
    onConfirmRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = ShadowColorSoft)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Tag
                Text(
                    text = "LEVEL $levelNumber",
                    color = BrightBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                // Modal Title
                Text(
                    text = "RETRY LEVEL?",
                    color = TextDeepNavy,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                // Message Description
                Text(
                    text = "Restart this level from the beginning? Your current moves and score for this attempt will be reset.",
                    color = TextSecondaryNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Retry CTA
                    GamingButton(
                        text = "RETRY",
                        icon = Icons.Rounded.Refresh,
                        onClick = onConfirmRetry,
                        gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        testTag = "retry_confirm_btn"
                    )

                    // Secondary Cancel Button
                    GamingButton(
                        text = "CANCEL",
                        icon = Icons.Rounded.Close,
                        onClick = onDismiss,
                        gradientColors = listOf(Color(0xFF90A4AE), Color(0xFF78909C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        testTag = "retry_cancel_btn"
                    )
                }
            }
        }
    }
}
