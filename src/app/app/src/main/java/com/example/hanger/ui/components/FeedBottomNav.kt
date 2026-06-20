package com.hanger.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedBottomNav(
    currentRoute: String,
    hasNotifications: Boolean = true,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onCameraClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HangerCream,
        shadowElevation = 0.dp
    ) {
        HorizontalDivider(color = Color(0x1A000000), thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Filled.Home,
                label = "Início",
                selected = currentRoute == "feed",
                onClick = onHomeClick
            )

            NavItem(
                icon = Icons.Filled.Explore,
                label = "Explorar",
                selected = currentRoute == "explore",
                onClick = onExploreClick
            )

            // Botão central de novo look
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(HangerPink)
                    .clickable(onClick = onCameraClick)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Novo look",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Atividade com badge de notificação
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onNotificationsClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (hasNotifications) {
                            Badge(containerColor = HangerPink)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Atividade",
                        tint = if (currentRoute == "notifications") HangerInk else HangerTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Atividade",
                    fontSize = 9.sp,
                    color = if (currentRoute == "notifications") HangerInk else HangerTextMuted,
                    letterSpacing = 0.3.sp,
                    fontWeight = if (currentRoute == "notifications") FontWeight.SemiBold else FontWeight.Normal
                )
            }

            NavItem(
                icon = Icons.Filled.AccountCircle,
                label = "Perfil",
                selected = currentRoute == "profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) HangerInk else HangerTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (selected) HangerInk else HangerTextMuted,
            letterSpacing = 0.3.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
