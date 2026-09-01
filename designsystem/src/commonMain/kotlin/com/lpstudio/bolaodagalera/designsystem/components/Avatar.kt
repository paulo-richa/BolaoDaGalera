package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.Neon

@Composable
fun UserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    fontSize: TextUnit = 14.sp,
    isOwner: Boolean = false,
    borderColor: Color = Neon
) {
    Box(
        modifier =
        modifier
            .size(size)
            .clip(CircleShape)
            .background(DeepNavy)
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isOwner) {
            Text("👑", fontSize = (size.value * 0.5).sp)
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Preview
@Composable
private fun UserAvatarPreview() {
    BolaoTheme {
        UserAvatar(initials = "PR")
    }
}

@Preview
@Composable
private fun UserAvatarOwnerPreview() {
    BolaoTheme {
        UserAvatar(initials = "PR", isOwner = true)
    }
}
