package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.PlayCircle
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
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.GamingButton
import com.example.ui.theme.*

@Composable
fun GameOverDialog(
    score: Int,
    onRetry: () -> Unit,
    onBackToMap: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "NO VALID MOVES",
                    color = BrightCoral,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "None of the remaining shapes can fit into the open grid tiles. Clear rows and columns to keep the board open!",
                    color = TextSecondaryNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Score pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SCORE: $score PTS",
                        color = Color(0xFFE65100),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Retry Button
                GamingButton(
                    text = "RETRY LEVEL",
                    icon = Icons.Rounded.Refresh,
                    onClick = onRetry,
                    gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    testTag = "gameover_retry_btn"
                )

                // Map Button
                GamingButton(
                    text = "BACK TO MAP",
                    icon = Icons.Rounded.Map,
                    onClick = onBackToMap,
                    gradientColors = listOf(Color(0xFF90A4AE), Color(0xFF78909C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    testTag = "gameover_map_btn"
                )
            }
        }
    }
}
