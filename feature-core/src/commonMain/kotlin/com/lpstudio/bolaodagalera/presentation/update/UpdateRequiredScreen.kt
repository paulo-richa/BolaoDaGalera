package com.lpstudio.bolaodagalera.presentation.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_core.generated.resources.Res
import bolaodagalera.feature_core.generated.resources.update_required_screen_button_update
import bolaodagalera.feature_core.generated.resources.update_required_screen_message
import bolaodagalera.feature_core.generated.resources.update_required_screen_title
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import org.jetbrains.compose.resources.stringResource

@Composable
fun UpdateRequiredScreen() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().background(DeepNavy).padding(BolaoSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BolaoIcon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Neon
        )

        Spacer(modifier = Modifier.height(40.dp))

        BolaoText(
            text = stringResource(Res.string.update_required_screen_title),
            color = Color.White,
            fontSize = BolaoTypography.displaySmall.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        BolaoText(
            text = stringResource(Res.string.update_required_screen_message),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = BolaoTypography.titleLarge.fontSize,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = BolaoSpacing.lg)
        )

        Spacer(modifier = Modifier.height(48.dp))

        BolaoButton(
            text = stringResource(Res.string.update_required_screen_button_update),
            onClick = {
                uriHandler.openUri("https://play.google.com/store/apps/details?id=com.lpstudio.bolaodagalera")
            }
        )
    }
}
